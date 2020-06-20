package com.dili.ss.java;

import org.springframework.core.env.Environment;

import java.util.List;

public interface BI {
    List<String> gif(String f);
    String gif(String f, String k);
    void daeif(String f, String k, Environment e);
}