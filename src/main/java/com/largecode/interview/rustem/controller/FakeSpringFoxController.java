package com.largecode.interview.rustem.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by r.zhunusov on 30.12.2015.
 * This is not real controller. Ignore it.
 * It's workaround to generate right set of Rest API for Asciidoctor without BasicErrorController.
 * It's happened on last day of project and I didn't find a direct solution quickly.
 * I will remove this class in next iteration. ;-)
 *
 */
//@RestController
@Api( hidden = true)
public class FakeSpringFoxController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    @ApiOperation(value = "", hidden = true)
    public String error() {
        return "My fake error handling.";
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}