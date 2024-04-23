/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.it.processor.getsse;



import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;
import org.apache.nifi.annotation.behavior.*;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;

@TriggerWhenEmpty
@Tags({"SSE, Serve Sent Event"})
@CapabilityDescription("Provides an GET Endpoint for Server Sent Events.")
@ReadsAttributes({@ReadsAttribute(attribute="none", description="Does not Read Attributes")})
@WritesAttributes({@WritesAttribute(attribute="Nothing yet...", description="TODO")})
public class GetSSE extends AbstractProcessor {
    //URL source of SSE
    public static final PropertyDescriptor URL = new PropertyDescriptor
            .Builder().name("URL")
            .displayName("URL")
            .description("URL of the SSE API emitter")
            .required(true)
            .addValidator(StandardValidators.URL_VALIDATOR)
            .build();

    public static final PropertyDescriptor API_KEY = new PropertyDescriptor
            .Builder().name("API_KEY")
            .displayName("TODO: API Key")
            .description("TODO: API Key of the SSE API emitter")
            .required(false)
            .build();

    public static final PropertyDescriptor BATCH_SIZE = new PropertyDescriptor.Builder()
            .name("Batch Size")
            .description("The maximum number of SSE emitter responses to pull in each iteration")
            .required(true)
            .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
            .defaultValue("10")
            .build();

    public static final Relationship RESPONSE = new Relationship.Builder()
            .name("Response")
            .description("Response FlowFiles transferred when receiving HTTP responses with a status code between 200 and 299.")
            .build();

    public static final Relationship FAILURE = new Relationship.Builder()
            .name("Failure")
            .description("Request FlowFiles transferred when receiving socket communication errors.")
            .build();

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;
    private Queue<InboundSseEvent> queue = new LinkedBlockingDeque<>();
    /*
    HERE BE BUGS
     */

    @Override
    protected void init(final ProcessorInitializationContext context){
        properties = new ArrayList<>();
        properties.add(URL);
        properties.add(BATCH_SIZE);
        this.properties = Collections.unmodifiableList(this.properties);

        relationships = new HashSet<>();
        relationships.add(RESPONSE);
        relationships.add(FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    private WebTarget target;
    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        Client client = ClientBuilder.newClient();
        target = client.target(context.getProperty(URL).getValue());
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        final int batchSize = context.getProperty(BATCH_SIZE).asInteger();
        final ComponentLog log = getLogger();

        ObjectMapper attributes = new ObjectMapper();
        FlowFile flowFile = session.get();

        try{
            try (SseEventSource source = SseEventSource.target(target).build()) {
                source.open();
                source.register(inboundSseEvent -> {
                    queue.add(inboundSseEvent);
                    handleEventQueue(batchSize, session, flowFile);
                });
            }
        }catch (final Exception e){
            log.error(e.getMessage());
            session.putAttribute(flowFile, "error", e.getMessage());
            session.transfer(flowFile, FAILURE);
        }
    }

    public void handleEventQueue(int batchSie, final ProcessSession session, FlowFile flowFile){
        if(queue.size() >= batchSie){
            for(int i = 0; i < batchSie; i++){
                InboundSseEvent inboundSseEvent = queue.poll();
                session.write(flowFile, outputStream -> {
                    outputStream.write(inboundSseEvent.getId().getBytes());
                    outputStream.write(inboundSseEvent.getName().getBytes());
                    outputStream.write(inboundSseEvent.getComment().getBytes());
                    outputStream.write(inboundSseEvent.readData().getBytes());
                });
                session.transfer(flowFile, RESPONSE);
            }
        }
    }
}
