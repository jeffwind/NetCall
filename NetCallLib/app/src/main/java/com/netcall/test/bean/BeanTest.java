package com.netcall.test.bean;

import com.netcall.IBean;

public class BeanTest implements IBean {
    //{"id":1,"content":"Hello, World!"}
    private int id;
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "BeanTest{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}
