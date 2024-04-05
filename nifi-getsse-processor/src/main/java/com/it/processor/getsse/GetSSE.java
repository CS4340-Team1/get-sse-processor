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



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.SseEventSource;
import org.apache.nifi.annotation.behavior.*;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
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

@TriggerWhenEmpty
@Tags({"example"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="asdf", description="asdf")})
@WritesAttributes({@WritesAttribute(attribute="asdf", description="asdf")})
public class GetSSE extends AbstractProcessor {

    public static final PropertyDescriptor URL = new PropertyDescriptor
            .Builder().name("URL")
            .displayName("url")
            .description("URL of the SSE emitter")
            .required(true)
            .addValidator(StandardValidators.URL_VALIDATOR)
            .build();

    public static final PropertyDescriptor BATCH_SIZE = new PropertyDescriptor.Builder()
            .name("Batch Size")
            .description("The maximum number of SSE emitter responses to pull in each iteration")
            .required(true)
            .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
            .defaultValue("10")
            .build();

    public static final Relationship ORIGINAL = new Relationship.Builder()
            .name("Original")
            .description("Request FlowFiles transferred when receiving HTTP responses with a status code between 200 and 299.")
            .build();

    public static final Relationship RESPONSE = new Relationship.Builder()
            .name("Response")
            .description("Response FlowFiles transferred when receiving HTTP responses with a status code between 200 and 299.")
            .build();

    public static final Relationship RETRY = new Relationship.Builder()
            .name("Retry")
            .description("Request FlowFiles transferred when receiving HTTP responses with a status code between 500 and 599.")
            .build();

    public static final Relationship NO_RETRY = new Relationship.Builder()
            .name("No Retry")
            .description("Request FlowFiles transferred when receiving HTTP responses with a status code between 400 an 499.")
            .build();

    public static final Relationship FAILURE = new Relationship.Builder()
            .name("Failure")
            .description("Request FlowFiles transferred when receiving socket communication errors.")
            .build();

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;

    /*
    HERE BE BUGS
     */

//    @Override
//    protected void init(final ProcessorInitializationContext context){
//        final List<PropertyDescriptor> properties = new ArrayList<>();
//        properties.add(URL);
//        properties.add(BATCH_SIZE);
//        this.properties = Collections.unmodifiableList(this.properties);
//
//        final Set<Relationship> relationships = new HashSet<>();
//        relationships.add(ORIGINAL);
//        relationships.add(RESPONSE);
//        relationships.add(RETRY);
//        relationships.add(NO_RETRY);
//        relationships.add(FAILURE);
//        this.relationships = Collections.unmodifiableSet(relationships);
//    }

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
        FlowFile responseFile = null;
        try{
            if (flowFile == null) {
                try (SseEventSource source = SseEventSource.target(target).build()) {
                    source.register(inboundSseEvent -> {
                        try{
                            session.putAllAttributes(responseFile, attributes.readValue(inboundSseEvent.readData(), Map.class));
                        } catch (JsonProcessingException e) {
                            log.error(e.getMessage());
                        }
                    });
                    source.open();
                }
            }
        }catch (final Exception e){
            log.error(e.getMessage());
        }
    }
}
