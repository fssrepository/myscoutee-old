package com.raxim.myscoutee.common.config

import com.raxim.myscoutee.profile.data.document.mongo.Event
import com.raxim.myscoutee.profile.data.document.mongo.EventItem
import com.raxim.myscoutee.profile.data.document.mongo.Feedback
import org.springframework.context.annotation.Configuration
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer

@Configuration
class RestDataConfig : RepositoryRestConfigurer {
    override fun configureRepositoryRestConfiguration(config: RepositoryRestConfiguration) {
        super.configureRepositoryRestConfiguration(config)
        config.exposeIdsFor(Event::class.java)
        config.exposeIdsFor(EventItem::class.java)
        config.exposeIdsFor(Feedback::class.java)
    }
}
