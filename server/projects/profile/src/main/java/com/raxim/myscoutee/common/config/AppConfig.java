package com.raxim.myscoutee.common.config

//import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.bohnman.squiggly.Squiggly
import com.github.bohnman.squiggly.web.RequestSquigglyContextProvider
import com.github.bohnman.squiggly.web.SquigglyRequestFilter
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.*
import javax.servlet.http.HttpServletRequest


@Configuration
@EnableConfigurationProperties
@EnableSpringDataWebSupport
@EnableNeo4jRepositories("com.raxim.myscoutee.profile.repository.neo4j")
@EntityScan("com.raxim.myscoutee.profile.data.document.neo4j")
@EnableMongoRepositories("com.raxim.myscoutee.profile.repository.mongo")
@EnableScheduling
class DataValidationConfig {

    @Bean
    fun localeResolver(): LocaleResolver {
        val slr = AcceptHeaderLocaleResolver()
        slr.defaultLocale = Locale.ENGLISH // Set default Locale as US
        return slr
    }

    @Bean
    fun messageSource(): MessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasenames(
            "classpath:i18n/messages"
        )
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    @Bean
    @Primary
    fun getValidator(): LocalValidatorFactoryBean {
        val bean = LocalValidatorFactoryBean()
        bean.setValidationMessageSource(messageSource())
        bean.afterPropertiesSet()
        return bean
    }

    @Bean
    fun squigglyRequestFilter(): FilterRegistrationBean<SquigglyRequestFilter> {
        val filter = FilterRegistrationBean<SquigglyRequestFilter>()
        filter.filter = SquigglyRequestFilter()
        filter.order = 1
        return filter
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, false)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        //TODO: the plugin needs to be extended for patch and post (it does not filter)
        return Squiggly.init(objectMapper, object : RequestSquigglyContextProvider() {
            override fun customizeFilter(filter: String, request: HttpServletRequest, beanClass: Class<*>): String {
                return if (request.method == HttpMethod.GET.name) {
                    val uriParts = request.requestURI.split("/")
                    val model = uriParts[uriParts.size - 1]
                    "_embedded[$model[$filter]],_links,page"
                } else {
                    "$filter,_links"
                }
            }
        })
    }
}