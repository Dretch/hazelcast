/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.spi.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.executor.DistributedExecutorService;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.instance.MemberImpl;
import com.hazelcast.nio.Address;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.OperationService;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(HazelcastParallelClassRunner.class)
@Category(QuickTest.class)
public class BasicOperationServiceTest extends HazelcastTestSupport {

    //there was a memory leak caused by the invocation not releasing the backup registration when there is a timeout.
    @Test
    public void testTimeoutSingleMember() throws InterruptedException {
        HazelcastInstance hz = createHazelcastInstance();
        final IQueue<Object> q = hz.getQueue("queue");

        for (int k = 0; k < 1000; k++) {
            Object response = q.poll(1, TimeUnit.MILLISECONDS);
            assertNull(response);
        }

        assertNoLitterInOpService(hz);
    }

    //there was a memory leak caused by the invocation not releasing the backup registration when there is a timeout.
    @Test
    public void testTimeoutWithMultiMemberCluster() throws InterruptedException {
        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        HazelcastInstance hz1 = factory.newHazelcastInstance();
        HazelcastInstance hz2 = factory.newHazelcastInstance();
        final IQueue<Object> q = hz1.getQueue("queue");

        for (int k = 0; k < 1000; k++) {
            Object response = q.poll(1, TimeUnit.MILLISECONDS);
            assertNull(response);
        }

        assertNoLitterInOpService(hz1);
        assertNoLitterInOpService(hz2);
    }

    //there was a memory leak caused by the invocation not releasing the backup registration
    // when Future.get() is not called.
    @Test
    public void testAsyncOpsSingleMember() {
        HazelcastInstance hz = createHazelcastInstance();
        final IMap<Object, Object> map = hz.getMap("test");

        final int count = 1000;
        for (int i = 0; i < count; i++) {
            map.putAsync(i, i);
        }

        assertTrueEventually(new AssertTask() {
            public void run() {
                assertEquals(count, map.size());
            }
        });

        assertNoLitterInOpService(hz);
    }

    //there was a memory leak caused by the invocation not releasing the backup registration
    // when Future.get() is not called.
    @Test
    public void testAsyncOpsMultiMember() throws InterruptedException {
        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        HazelcastInstance hz = factory.newHazelcastInstance();
        HazelcastInstance hz2 = factory.newHazelcastInstance();
        warmUpPartitions(hz2, hz);

        final IMap<Object, Object> map = hz.getMap("test");
        final IMap<Object, Object> map2 = hz2.getMap("test");

        final int count = 2000;
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                map.putAsync(i, i);
            } else {
                map2.putAsync(i, i);
            }
        }

        assertTrueEventually(new AssertTask() {
            public void run() {
                assertEquals(count, map.size());
                assertEquals(count, map2.size());
            }
        });

        assertNoLitterInOpService(hz);
        assertNoLitterInOpService(hz2);
    }

    @Test(expected = ExecutionException.class)
    public void testPropagateSerializationErrorOnResponseToCallerGithubIssue2559()
            throws Exception {

        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        HazelcastInstance hz1 = factory.newHazelcastInstance();
        HazelcastInstance hz2 = factory.newHazelcastInstance();

        Field original = HazelcastInstanceProxy.class.getDeclaredField("original");
        original.setAccessible(true);

        HazelcastInstanceImpl impl = (HazelcastInstanceImpl) original.get(hz1);
        OperationService operationService = impl.node.nodeEngine.getOperationService();

        Address address = ((MemberImpl) hz2.getCluster().getLocalMember()).getAddress();

        Operation operation = new GithubIssue2559Operation();
        String serviceName = DistributedExecutorService.SERVICE_NAME;
        InvocationBuilder invocationBuilder = operationService.createInvocationBuilder(serviceName, operation, address);
        invocationBuilder.invoke().get();
    }

    public static class GithubIssue2559Operation
            extends Operation {

        private GithubIssue2559Value value;

        @Override
        public void beforeRun()
                throws Exception {
        }

        @Override
        public void run()
                throws Exception {

            value = new GithubIssue2559Value();
            value.foo = 10;
        }

        @Override
        public void afterRun()
                throws Exception {
        }

        @Override
        public boolean returnsResponse() {
            return true;
        }

        @Override
        public Object getResponse() {
            return value;
        }

        @Override
        protected void writeInternal(ObjectDataOutput out)
                throws IOException {

        }

        @Override
        protected void readInternal(ObjectDataInput in)
                throws IOException {

        }
    }

    public static class GithubIssue2559Value
            implements DataSerializable {

        private int foo;

        @Override
        public void writeData(ObjectDataOutput out)
                throws IOException {

            throw new RuntimeException("BAM!");
        }

        @Override
        public void readData(ObjectDataInput in)
                throws IOException {
            foo = in.readInt();
        }
    }

    private void assertNoLitterInOpService(HazelcastInstance hz) {
        final BasicOperationService operationService = (BasicOperationService) getNode(hz).nodeEngine.getOperationService();

        //we need to do this with an assertTrueEventually because it can happen that system calls are being send
        //and this leads to the maps not being empty. But eventually they will be empty at some moment in time.
        assertTrueEventually(new AssertTask() {
            @Override
            public void run() {
                assertEquals("invocations should be empty", 0, operationService.invocations.size());
             }
        });
    }
}
