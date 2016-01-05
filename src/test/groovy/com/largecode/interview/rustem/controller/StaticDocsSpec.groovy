package com.largecode.interview.rustem.controller

import com.largecode.interview.rustem.Application
import groovy.io.FileType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.support.DirtiesContextTestExecutionListener
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Shared
import springfox.documentation.staticdocs.Swagger2MarkupResultHandler

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Created by r.zhunusov on 30.12.2015.
 */
@ContextConfiguration(
        loader = SpringApplicationContextLoader,
        classes = Application)
@WebAppConfiguration
@TestExecutionListeners([DependencyInjectionTestExecutionListener, DirtiesContextTestExecutionListener])
class StaticDocsSpec extends spock.lang.Specification {

//    @Autowired
//    WebApplicationContext context;
    @Shared
    @AutoCleanup
    WebApplicationContext context


    void setupSpec() {
        Future future = Executors
                .newSingleThreadExecutor().submit(
                new Callable() {
                    @Override
                    public ConfigurableApplicationContext call() throws Exception {
                        return (WebApplicationContext) SpringApplication
                                .run(Application.class)
                    }
                })
        context = future.get(60, TimeUnit.SECONDS)
    }


    MockMvc mockMvc;


    def setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build()
    }


    def "generates api asciidoc for the VoteOfLunch "() {
        setup:
            String outDir = System.getProperty('asciiDocOutputDir', 'build/aciidoc')
            Swagger2MarkupResultHandler resultHandler = Swagger2MarkupResultHandler
                    .outputDirectory(outDir)
                    .build()

        when:
            this.mockMvc.perform(get("/v2/api-docs").accept(MediaType.APPLICATION_JSON))
                    .andDo(resultHandler)
                    .andExpect(status().isOk())
        then:
            def list = []
            def dir = new File(resultHandler.outputDir)
            dir.eachFileRecurse(FileType.FILES) { file ->
                list << file.name
            }
            list.sort() == ['definitions.adoc', 'overview.adoc', 'paths.adoc']
    }
}
