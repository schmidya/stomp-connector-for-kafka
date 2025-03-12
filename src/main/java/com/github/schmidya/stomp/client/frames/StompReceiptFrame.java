package com.github.schmidya.stomp.client.frames;

import java.util.Map;

public class StompReceiptFrame extends StompServerFrame {

    protected StompReceiptFrame(Map<String, String> headers) {
        super(CMD_RECEIPT, headers, "");
        // TODO Auto-generated constructor stub
    }

}
