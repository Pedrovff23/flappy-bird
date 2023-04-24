package com.jogo.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class jogo extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture[] passaros;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture fundo;
    private Texture gameOver;

    //Atributos de configuraçoes
    private float larguraDispositivo;
    private float alturaDispositivo;
    private float variacao = 0;
    private float gravidade = 0;
    private float posicaoPassaroY = 0;
    private float posicaoCanoHorizontal = 0;
    private float posicaoCanoVertical = 0;
    private float espacoEntreCanos;
    private Random random;

    //Exibição de textos
    BitmapFont textoPontuacao;
    BitmapFont textoReniciar;
    BitmapFont textoMelhorPontuacao;
    private int pontos = 0;
    private int pontuacaoMaxima = 0;
    boolean passouCano = false;

    //Formas para a colisão
    private Circle circuloPassaro;
    private Rectangle retanguloCanoCima;
    private Rectangle retanguloCanoBaixo;
    private int estadoJogo = 0;

    //Configuração dos sons
    Sound somVoando;
    Sound somColisao;
    Sound somPontuacao;

    //Objeto salvar pontuação
    Preferences preferences;

    //Objeto para Câmera
    private OrthographicCamera camera;
    private Viewport viewport;


    @SuppressWarnings("SuspiciousIndentation")
    @Override
    public void create() {
        inicializarObjetos();
        inicializarTextura();
    }

    @Override
    public void render() {
        //Limpar os frames anteriores
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        verificarEstadoJogo();
        validarPontos();
        desenharTexturas();
        detectarColisoes();
        }

    private void verificarEstadoJogo(){

        if(estadoJogo == 0){

            // Aplica evento de toque na tela;
            if(Gdx.input.justTouched()){
                gravidade = -15;
                estadoJogo = 1;
                somVoando.play();
            }

        } else if (estadoJogo == 1) {

            // Aplica evento de toque na tela;
            if(Gdx.input.justTouched()){
                gravidade = -15;
                somVoando.play();
            }

            // Aplica movimento do cano;
            posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 400;
            if(posicaoCanoHorizontal < -canoTopo.getWidth()){
                posicaoCanoHorizontal = larguraDispositivo;
                posicaoCanoVertical = random.nextInt(800) -400;
                passouCano = false;
            }

            // Aplica gravidade no pássaro;
            if(posicaoPassaroY > 0 || gravidade < 0)
                posicaoPassaroY = posicaoPassaroY - gravidade;

            gravidade ++;

        }else if (estadoJogo == 2) {

            if(pontos > pontuacaoMaxima){
                pontuacaoMaxima = pontos;
                preferences.putInteger("pontuacaoMaxima",pontuacaoMaxima);
            }

            if(posicaoPassaroY > 0 || gravidade < 0)
                posicaoPassaroY = posicaoPassaroY - gravidade;
            gravidade++;

            if(Gdx.input.justTouched()){
                estadoJogo = 0;
                pontos = 0;
                gravidade = 0;
                posicaoPassaroY = alturaDispositivo/2;
                posicaoCanoHorizontal = larguraDispositivo;
            }

        }

        /*
        * 0 - Jogo iniciado, passaro parado
        * 1 - Começa o jogo
        * 2 - Colidiu
         */

        }

        private void validarPontos(){

        if(posicaoCanoHorizontal < 50 - passaros[0].getHeight()){ //Passou posição do passaro
            if(!passouCano){
                pontos++;
                passouCano = true;
                somPontuacao.play();
            }
        }
            variacao += Gdx.graphics.getDeltaTime() * 10;
            // Verifica variação para bater asas do pássaro.
            if(variacao > 3)
                variacao = 0;

        }

    private void detectarColisoes() {
        circuloPassaro.set(
                50 + (float)passaros[0].getWidth()/2,
                posicaoPassaroY + (float) passaros[0].getHeight()/2,
                (float) passaros[0].getWidth()/2
        );
        retanguloCanoBaixo.set(
                posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical,
                canoBaixo.getWidth(), canoBaixo.getHeight()
        );
        retanguloCanoCima.set(
                posicaoCanoHorizontal,alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
                canoTopo.getWidth(), canoTopo.getHeight()
        );

        boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro,retanguloCanoCima);
        boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro,retanguloCanoBaixo);
        if(colidiuCanoBaixo || colidiuCanoCima){
            if(estadoJogo == 1){
                somColisao.play();
                estadoJogo = 2;
            }

        }

    }

        private void desenharTexturas(){
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
            batch.draw(fundo,0,0,larguraDispositivo,alturaDispositivo);
            batch.draw(passaros[(int)variacao],50,posicaoPassaroY);
            batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical);
            batch.draw(canoTopo, posicaoCanoHorizontal,alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical );
            textoPontuacao.draw(batch,String.valueOf(pontos),larguraDispositivo/2,alturaDispositivo - 100);

            if(estadoJogo == 2){
                batch.draw(gameOver,
                        larguraDispositivo/2 - (float) gameOver.getWidth()/2,alturaDispositivo/2);
                textoReniciar.draw(batch,"Toque para reniciar!", larguraDispositivo/2 -140,
                        alturaDispositivo / 2 -(float) gameOver.getHeight() /2);
                textoMelhorPontuacao.draw(batch,"Seu record é: " + pontuacaoMaxima + " Pontos",
                        larguraDispositivo/2 -140,alturaDispositivo / 2 -(float) gameOver.getHeight());
            }
        batch.end();
        }
        private void inicializarTextura(){
            passaros = new Texture[3];
            passaros[0] = new Texture("passaro1.png");
            passaros[1] = new Texture("passaro2.png");
            passaros[2] = new Texture("passaro3.png");

            fundo = new Texture("fundo.png");
            canoBaixo = new Texture("cano_baixo_maior.png");
            canoTopo = new Texture("cano_topo_maior.png");
            gameOver = new Texture("game_over.png");

        }

        private void inicializarObjetos(){
            batch = new SpriteBatch();
            random = new Random();

            float VIRTUAL_WIDTH = 720;
            larguraDispositivo = VIRTUAL_WIDTH;
            float VIRTUAL_HEITH = 1280;
            alturaDispositivo = VIRTUAL_HEITH;
            posicaoPassaroY = alturaDispositivo/2;
            posicaoCanoHorizontal = larguraDispositivo;
            espacoEntreCanos = 350;

            //Configuração dos Textos
            textoPontuacao = new BitmapFont();
            textoPontuacao.setColor(Color.WHITE);
            textoPontuacao.getData().setScale(10);

            textoReniciar = new BitmapFont();
            textoReniciar.setColor(Color.GREEN);
            textoReniciar.getData().setScale(2);

            textoMelhorPontuacao = new BitmapFont();
            textoMelhorPontuacao.setColor(Color.RED);
            textoMelhorPontuacao.getData().setScale(2);

            //Formas Geoamétricas para colisoes;
            circuloPassaro = new Circle();
            retanguloCanoCima = new Rectangle();
            retanguloCanoBaixo = new Rectangle();

            //Inicalizar sons
            somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
            somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
            somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

            //Configurar preferências dos objetos;
            preferences = Gdx.app.getPreferences("flappyBird");
            pontuacaoMaxima = preferences.getInteger("pontuacaoMaxima",0);

            //Configuração da câmera
            camera  = new OrthographicCamera();
            camera.position.set(VIRTUAL_WIDTH /2, VIRTUAL_HEITH /2,0);
            viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEITH, camera);

        }

    @Override
    public void resize(int width, int height) {
        viewport.update(width,height);
    }

    @Override
    public void dispose() {
    }
}
