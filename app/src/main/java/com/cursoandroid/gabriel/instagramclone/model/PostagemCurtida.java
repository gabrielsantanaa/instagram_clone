package com.cursoandroid.gabriel.instagramclone.model;

public class PostagemCurtida {

    public int qtdCurtida = 0;
    public Feed feed;
    public Usuario usuario;

    public PostagemCurtida() {
    }

    public void salvar(){
    }

    public void atualizarQtd(int valor){

    }

    public void remover(){
    }

    public int getQtdCurtida() {
        return qtdCurtida;
    }

    public void setQtdCurtida(int qtdCurtida) {
        this.qtdCurtida = qtdCurtida;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
