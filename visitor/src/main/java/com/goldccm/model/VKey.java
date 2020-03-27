package com.goldccm.model;

/**
 * @program: goldccm
 * @description: keymodel
 * @author: cwf
 * @create: 2020-03-25 09:59
 **/
public class VKey {

    private String publicKey;
    private String privateKey;

    public VKey() {
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
