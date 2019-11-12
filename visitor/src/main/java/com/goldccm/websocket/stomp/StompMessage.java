package com.goldccm.websocket.stomp;



/**
 * @author linyun
 * @date 2018/9/13 下午5:44
 */

public class StompMessage {
    private String to;
    private Long date;
    private String from;
    private String content;



    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

}
