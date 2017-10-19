/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package org.apache.logging.log4j.core.async;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.ThreadContext.ContextStack;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.TimestampMessage;
import org.apache.logging.log4j.spi.MutableThreadContextStack;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the RingBufferLogEvent class.
 */
public class RingBufferLogEventTest {

    @Test
    public void testGetLevelReturnsOffIfNullLevelSet() {
        final RingBufferLogEvent evt = new RingBufferLogEvent();
        final String loggerName = null;
        final Marker marker = null;
        final String fqcn = null;
        final Level level = null;
        final Message data = null;
        final Throwable t = null;
        final Map<String, String> map = null;
        final ContextStack contextStack = null;
        final String threadName = null;
        final StackTraceElement location = null;
        final long currentTimeMillis = 0;
        evt.setValues(null, loggerName, marker, fqcn, level, data, t, map,
                contextStack, threadName, location, currentTimeMillis);
        assertEquals(Level.OFF, evt.getLevel());
    }

    @Test
    public void testGetMessageReturnsNonNullMessage() {
        final RingBufferLogEvent evt = new RingBufferLogEvent();
        final String loggerName = null;
        final Marker marker = null;
        final String fqcn = null;
        final Level level = null;
        final Message data = null;
        final Throwable t = null;
        final Map<String, String> map = null;
        final ContextStack contextStack = null;
        final String threadName = null;
        final StackTraceElement location = null;
        final long currentTimeMillis = 0;
        evt.setValues(null, loggerName, marker, fqcn, level, data, t, map,
                contextStack, threadName, location, currentTimeMillis);
        assertNotNull(evt.getMessage());
    }

    @Test
    public void testGetMillisReturnsConstructorMillisForNormalMessage() {
        final RingBufferLogEvent evt = new RingBufferLogEvent();
        final String loggerName = null;
        final Marker marker = null;
        final String fqcn = null;
        final Level level = null;
        final Message data = null;
        final Throwable t = null;
        final Map<String, String> map = null;
        final ContextStack contextStack = null;
        final String threadName = null;
        final StackTraceElement location = null;
        final long currentTimeMillis = 123;
        evt.setValues(null, loggerName, marker, fqcn, level, data, t, map,
                contextStack, threadName, location, currentTimeMillis);
        assertEquals(123, evt.getTimeMillis());
    }

    static class TimeMsg implements Message, TimestampMessage {
        private static final long serialVersionUID = -2038413535672337079L;
        private final String msg;
        private final long timestamp;

        public TimeMsg(final String msg, final long timestamp) {
            this.msg = msg;
            this.timestamp = timestamp;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String getFormattedMessage() {
            return msg;
        }

        @Override
        public String getFormat() {
            return null;
        }

        @Override
        public Object[] getParameters() {
            return null;
        }

        @Override
        public Throwable getThrowable() {
            return null;
        }
    }

    @Test
    public void testGetMillisReturnsMsgTimestampForTimestampMessage() {
        final RingBufferLogEvent evt = new RingBufferLogEvent();
        final String loggerName = null;
        final Marker marker = null;
        final String fqcn = null;
        final Level level = null;
        final Message data = new TimeMsg("", 567);
        final Throwable t = null;
        final Map<String, String> map = null;
        final ContextStack contextStack = null;
        final String threadName = null;
        final StackTraceElement location = null;
        final long currentTimeMillis = 123;
        evt.setValues(null, loggerName, marker, fqcn, level, data, t, map,
                contextStack, threadName, location, currentTimeMillis);
        assertEquals(567, evt.getTimeMillis());
    }

    @Test
    public void testSerializationDeserialization() throws IOException, ClassNotFoundException {
        final RingBufferLogEvent evt = new RingBufferLogEvent();
        final String loggerName = "logger.name";
        final Marker marker = null;
        final String fqcn = "f.q.c.n";
        final Level level = Level.TRACE;
        final Message data = new SimpleMessage("message");
        final Throwable t = new InternalError("not a real error");
        final Map<String, String> map = null;
        final ContextStack contextStack = null;
        final String threadName = "main";
        final StackTraceElement location = null;
        final long currentTimeMillis = 12345;
        evt.setValues(null, loggerName, marker, fqcn, level, data, t, map,
                contextStack, threadName, location, currentTimeMillis);
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(evt);
        
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        final RingBufferLogEvent other = (RingBufferLogEvent) in.readObject();
        assertEquals(loggerName, other.getLoggerName());
        assertEquals(marker, other.getMarker());
        assertEquals(fqcn, other.getLoggerFqcn());
        assertEquals(level, other.getLevel());
        assertEquals(data, other.getMessage());
        assertNull("null after serialization", other.getThrown());
        assertEquals(new ThrowableProxy(t), other.getThrownProxy());
        assertEquals(map, other.getContextMap());
        assertEquals(contextStack, other.getContextStack());
        assertEquals(threadName, other.getThreadName());
        assertEquals(location, other.getSource());
        assertEquals(currentTimeMillis, other.getTimeMillis());
    }
    
    @Test
    public void testCreateMementoReturnsCopy() {
        final RingBufferLogEvent evt = new RingBufferLogEvent();
        final String loggerName = "logger.name";
        final Marker marker = MarkerManager.getMarker("marked man");
        final String fqcn = "f.q.c.n";
        final Level level = Level.TRACE;
        final Message data = new SimpleMessage("message");
        final Throwable t = new InternalError("not a real error");
        final Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        final ContextStack contextStack = new MutableThreadContextStack(Arrays.asList("a", "b"));
        final String threadName = "main";
        final StackTraceElement location = null;
        final long currentTimeMillis = 12345;
        evt.setValues(null, loggerName, marker, fqcn, level, data, t, map,
                contextStack, threadName, location, currentTimeMillis);
        
        final LogEvent actual = evt.createMemento();
        assertEquals(evt.getLoggerName(), actual.getLoggerName());
        assertEquals(evt.getMarker(), actual.getMarker());
        assertEquals(evt.getLoggerFqcn(), actual.getLoggerFqcn());
        assertEquals(evt.getLevel(), actual.getLevel());
        assertEquals(evt.getMessage(), actual.getMessage());
        assertEquals(evt.getThrown(), actual.getThrown());
        assertEquals(evt.getContextMap(), actual.getContextMap());
        assertEquals(evt.getContextStack(), actual.getContextStack());
        assertEquals(evt.getThreadName(), actual.getThreadName());
        assertEquals(evt.getTimeMillis(), actual.getTimeMillis());
        assertEquals(evt.getSource(), actual.getSource());
        assertEquals(evt.getThrownProxy(), actual.getThrownProxy());
    }
}