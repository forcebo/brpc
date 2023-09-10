package com.lwb.impl;

import com.lwb.HelloBRpc;

public class HelloBRpcImpl implements HelloBRpc {
    @Override
    public String sayHi(String message) {
        return "hi consumer:" + message;
    }
}
