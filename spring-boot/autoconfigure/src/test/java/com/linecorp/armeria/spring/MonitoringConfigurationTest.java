/*
 * Copyright 2017 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.spring;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.codahale.metrics.MetricRegistry;

import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponseWriter;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.AbstractHttpService;
import com.linecorp.armeria.server.PathMapping;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.logging.LoggingService;
import com.linecorp.armeria.spring.ArmeriaAutoConfigurationTest.TestConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
@ActiveProfiles({ "local", "autoConfTest" })
@DirtiesContext
public class MonitoringConfigurationTest {

    @SpringBootApplication
    public static class TestConfiguration {

        @Bean
        public HttpServiceRegistrationBean okService() {
            return new HttpServiceRegistrationBean()
                    .setServiceName("okService")
                    .setService(new AbstractHttpService() {
                        @Override
                        protected void doGet(ServiceRequestContext ctx, HttpRequest req,
                                             HttpResponseWriter res) throws Exception {
                            res.respond(HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8, "ok");
                        }
                    })
                    .setPathMapping(PathMapping.ofExact("/ok"))
                    .setDecorator(LoggingService.newDecorator());
        }
    }

    @Inject
    private MetricRegistry metricRegistry;

    @Test
    public void testJvmMetrics() throws Exception {
        assertThat(metricRegistry.getMetrics().get("jvm.buffers.direct.count")).isNotNull();
        assertThat(metricRegistry.getMetrics().get("jvm.classloader.loaded")).isNotNull();
        assertThat(metricRegistry.getMetrics().get("jvm.memory.heap.max")).isNotNull();
        assertThat(metricRegistry.getMetrics().get("jvm.threads.daemon.count")).isNotNull();
        assertThat(metricRegistry.getMetrics().get("system.cpu.load-average")).isNotNull();
    }
}
