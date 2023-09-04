package com.lwb;

public class HelloBRpcImpl implements HelloBRpc{
    @Override
    public String sayHi(String message) {
        return "hi consumer:" + message;
    }
}
