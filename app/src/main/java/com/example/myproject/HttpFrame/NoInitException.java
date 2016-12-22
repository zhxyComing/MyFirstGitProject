package com.example.myproject.HttpFrame;

/**
 * Created by 徐政 on 2016/11/26.
 */
public class NoInitException extends Throwable {
    public NoInitException() {
        super("HttpManager no init!");
    }
}
