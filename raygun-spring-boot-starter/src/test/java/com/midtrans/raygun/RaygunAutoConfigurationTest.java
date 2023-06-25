/*
 * Copyright 2022-2023 authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.midtrans.raygun;

import static org.assertj.core.api.Assertions.assertThat;

import com.mindscapehq.raygun4java.core.RaygunClientFactory;
import com.mindscapehq.raygun4java.core.RaygunSettings;
import com.mindscapehq.raygun4java.core.messages.RaygunMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Set;

/**
 * Test for {@link RaygunAutoConfiguration}.
 *
 * @author Raydhitya Yoseph
 */
class RaygunAutoConfigurationTest {

  @Nested
  class WhenApiKey {

    @Test
    void notConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .run(
              context -> {
                assertThat(context.getBean(RaygunProperties.class).getApiKey()).isNull();
              });
    }

    @Test
    void configured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withPropertyValues("raygun.api-key=api-key")
          .run(
              context -> {
                assertThat(context.getBean(RaygunProperties.class).getApiKey())
                    .isEqualTo("api-key");
              });
    }
  }

  @Nested
  class WhenProxy {

    @BeforeEach
    void beforeEach() {
      RaygunSettings.getSettings().setHttpProxy(null, 0);
    }

    @Test
    void notConfiguredShouldNotSetProxy() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .run(
              context -> {
                assertThat(RaygunSettings.getSettings().getHttpProxy()).isNull();
              });
    }

    @Test
    void configuredShouldSetProxy() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withPropertyValues("raygun.proxy.host=host", "raygun.proxy.port=3128")
          .run(
              context -> {
                assertThat(RaygunSettings.getSettings().getHttpProxy()).isNotNull();
              });
    }
  }

  @Nested
  class WhenVersion {

    @Test
    void notConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .run(
              context -> {
                RaygunClientFactory clientFactory = context.getBean(RaygunClientFactory.class);
                RaygunMessage message = clientFactory.newClient().buildMessage(null, null, null);
                assertThat(message.getDetails().getVersion()).isEqualTo("Not supplied");
              });
    }

    @Test
    void configured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withPropertyValues("raygun.version=0.7.0")
          .run(
              context -> {
                RaygunClientFactory clientFactory = context.getBean(RaygunClientFactory.class);
                RaygunMessage message = clientFactory.newClient().buildMessage(null, null, null);
                assertThat(message.getDetails().getVersion()).isEqualTo("0.7.0");
              });
    }
  }

  @Nested
  class WhenTags {

    @Test
    void notConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .run(
              context -> {
                assertThat(context.getBean(RaygunClientFactory.class).getTags()).isEmpty();
              });
    }

    @Test
    void configured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withPropertyValues("raygun.tags=poppinparty,morfonica")
          .run(
              context -> {
                assertThat(context.getBean(RaygunClientFactory.class).getTags())
                    .isEqualTo(Set.of("poppinparty", "morfonica"));
              });
    }

    @Test
    void duplicateConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withPropertyValues("raygun.tags=poppinparty,morfonica,poppinparty,morfonica")
          .run(
              context -> {
                assertThat(context.getBean(RaygunClientFactory.class).getTags())
                    .isEqualTo(Set.of("poppinparty", "morfonica"));
              });
    }
  }

  @Nested
  class WhenConnectTimeout {

    @Test
    void notConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .run(
              context -> {
                assertThat(RaygunSettings.getSettings().getConnectTimeout())
                    .isEqualTo(Integer.valueOf(10000));
              });
    }

    @Test
    void configured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withPropertyValues("raygun.connect-timeout=1000")
          .run(
              context -> {
                assertThat(RaygunSettings.getSettings().getConnectTimeout())
                    .isEqualTo(Integer.valueOf(1000));
              });
    }
  }

  @Nested
  class WhenRaygunTemplateBean {

    @Test
    void notConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .run(
              context -> {
                assertThat(context).hasSingleBean(RaygunTemplate.class);
                assertThat(context.getBean(RaygunTemplate.class).getTaskExecutor()).isNotNull();
              });
    }

    @Test
    void configured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withConfiguration(UserConfigurations.of(RaygunConfiguration.class))
          .run(
              context -> {
                assertThat(context).hasSingleBean(RaygunTemplate.class);
                assertThat(context.getBean(RaygunTemplate.class).getTaskExecutor()).isNull();
              });
    }
  }

  @Nested
  class WhenTaskExecutorBean {

    @Test
    /** User excludes TaskExecutionAutoConfiguration which is included by default. */
    void notConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .run(
              context -> {
                assertThat(context).doesNotHaveBean(ThreadPoolTaskExecutor.class);
                assertThat(context.getBean(RaygunTemplate.class).getTaskExecutor())
                    .isInstanceOf(SyncTaskExecutor.class);
              });
    }

    @Test
    void singleConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withConfiguration(AutoConfigurations.of(TaskExecutionAutoConfiguration.class))
          .run(
              context -> {
                assertThat(context).hasSingleBean(ThreadPoolTaskExecutor.class);
                assertThat(context.getBean(RaygunTemplate.class).getTaskExecutor())
                    .isInstanceOf(ThreadPoolTaskExecutor.class);
              });
    }

    @Test
    void multipleConfiguredAndRaygunTaskExecutorBeanNotConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withConfiguration(UserConfigurations.of(MultipleTaskExecutorsConfiguration.class))
          .run(
              context -> {
                assertThat(context).hasBean("taskExecutor1");
                assertThat(context).hasBean("taskExecutor2");
                assertThat(context).doesNotHaveBean("raygunTaskExecutor");
                assertThat(context.getBean(RaygunTemplate.class).getTaskExecutor())
                    .isInstanceOf(SyncTaskExecutor.class);
              });
    }

    @Test
    void multipleConfiguredAndRaygunTaskExecutorBeanConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withConfiguration(UserConfigurations.of(MultipleTaskExecutorsConfiguration.class))
          .withConfiguration(UserConfigurations.of(RaygunTaskExecutorConfiguration.class))
          .run(
              context -> {
                assertThat(context).hasBean("taskExecutor1");
                assertThat(context).hasBean("taskExecutor2");
                assertThat(context).hasBean("raygunTaskExecutor");
                assertThat(context.getBean(RaygunTemplate.class).getTaskExecutor())
                    .isInstanceOf(ThreadPoolTaskExecutor.class);
              });
    }
  }

  @Nested
  class WhenRaygunExceptionExcludeRegistrar {

    @Test
    void notConfigured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .run(
              context -> {
                assertThat(context).hasSingleBean(RaygunExceptionExcludeRegistrar.class);
                assertThat(context).hasBean("defaultRaygunExceptionExcludeRegistrar");
              });
    }

    @Test
    void configured() {
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(RaygunAutoConfiguration.class))
          .withConfiguration(
              UserConfigurations.of(RaygunExceptionExcludeRegistrarConfiguration.class))
          .run(
              context -> {
                assertThat(context).hasSingleBean(RaygunExceptionExcludeRegistrar.class);
                assertThat(context).hasBean("userRaygunExceptionExcludeRegistrar");
              });
    }
  }

  @Configuration
  static class RaygunConfiguration {

    @Bean
    RaygunTemplate raygunTemplate() {
      return new RaygunTemplate(null, null);
    }
  }

  @Configuration
  static class MultipleTaskExecutorsConfiguration {

    @Bean
    TaskExecutor taskExecutor1() {
      return new ThreadPoolTaskExecutor();
    }

    @Bean
    TaskExecutor taskExecutor2() {
      return new ThreadPoolTaskExecutor();
    }
  }

  @Configuration
  static class RaygunTaskExecutorConfiguration {

    @Bean
    TaskExecutor raygunTaskExecutor() {
      return new ThreadPoolTaskExecutor();
    }
  }

  @Configuration
  static class RaygunExceptionExcludeRegistrarConfiguration {

    @Bean
    RaygunExceptionExcludeRegistrar userRaygunExceptionExcludeRegistrar() {
      return new RaygunExceptionExcludeRegistrar() {

        @Override
        public void registerExceptions(RaygunExceptionExcludeRegistry registry) {
          registry.registerException(Exception.class);
        }
      };
    }
  }
}
