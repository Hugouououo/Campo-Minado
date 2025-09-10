package com.hugouououo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class CampoMinado
{
    private static class Blocos extends JButton
    {
        public int l;
        public int c;

        public Blocos(int l, int c){
            this.l = l;
            this.c = c;
        }
    }

    int tamBlocos = 70;
    int numLinhas = 10;
    int numColunas = numLinhas;
    int tabLargura = numColunas * tamBlocos;
    int tabAltura = numLinhas * tamBlocos;

    ImageIcon iconeBomba;
    ImageIcon iconeBandeira;
    //ImageIcon iconeVazio;

    JFrame frame = new JFrame("Campo Minado");

    JLabel textoTitulo = new JLabel();
    JPanel painelTabuleiro = new JPanel();
    JLabel textoCronometro = new JLabel();
    Timer cronometro;

    JButton botaoReiniciar = new JButton("🔁");

    int quantMinas = 10;
    int bandeirasSobrando = quantMinas;

    Blocos[][] tabuleiro = new Blocos[numLinhas][numColunas];
    ArrayList<Blocos> listaMinas;
    Random aleatorio = new Random();

    int blocosClicados = 0;
    boolean gameOver = false;

    int minutos = 0;
    int segundos = 0;

    public void iniciarCronometro() {
        cronometro = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                segundos++;
                if (segundos == 60) {
                    segundos = 0;
                    minutos++;
                }
                String tempoFormatado = String.format("%02d:%02d", minutos, segundos);
                textoCronometro.setText(tempoFormatado);
            }
        });
        cronometro.start();
    }

    CampoMinado()
    {
        try {
            iconeBomba = new ImageIcon(new ImageIcon(getClass().getResource("/imagens/iconeBomba.png"))
                    .getImage().getScaledInstance(tamBlocos - 20, tamBlocos - 20, Image.SCALE_SMOOTH));
            iconeBandeira = new ImageIcon(new ImageIcon(getClass().getResource("/imagens/iconeBandeira.png"))
                    .getImage().getScaledInstance(tamBlocos - 20, tamBlocos - 20, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagens: " + e.getMessage());
            iconeBomba = null;
            iconeBandeira = null;
        }

        frame.setSize(tabLargura, tabAltura);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel painelCabecalho = new JPanel(new BorderLayout());

        textoTitulo.setFont(new Font("Helvetica", Font.BOLD, 25));
        textoTitulo.setHorizontalAlignment(JLabel.CENTER);
        textoTitulo.setText("Bandeiras disponíveis: " + Integer.toString(bandeirasSobrando));
        textoTitulo.setOpaque(true);

        textoCronometro.setFont(new Font("Helvetica", Font.BOLD, 25));
        textoCronometro.setHorizontalAlignment(JLabel.LEFT);
        textoCronometro.setText("00:00");
        textoCronometro.setOpaque(true);

        painelCabecalho.add(botaoReiniciar, BorderLayout.WEST);
        painelCabecalho.add(textoTitulo, BorderLayout.CENTER);
        painelCabecalho.add(textoCronometro, BorderLayout.EAST);
        frame.add(painelCabecalho, BorderLayout.NORTH);

        botaoReiniciar.setToolTipText("Reiniciar jogo");

        painelTabuleiro.setLayout(new GridLayout(numLinhas, numColunas));
        frame.add(painelTabuleiro);

        for (int l = 0; l < numLinhas; l++) {
            for (int c = 0; c < numColunas; c++) {
                Blocos bloco = new Blocos(l, c);
                tabuleiro[l][c] = bloco;

                bloco.setFocusable(false);
                bloco.setMargin(new Insets(0, 0, 0, 0));
                bloco.setFont(new Font("Arial Unicode MS", Font.PLAIN, 45));

                bloco.setBackground(new Color(230, 230, 230));

                bloco.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {

                        if(gameOver){
                            return;
                        }

                        Blocos bloco = (Blocos) e.getSource();

                        if(e.getButton() == MouseEvent.BUTTON1){
                            // Agora verifica se o ícone é nulo antes de revelar
                            if (bloco.getIcon() == null && bloco.getText().isEmpty()){
                                if (listaMinas.contains(bloco))
                                    revelarMinas();
                                else
                                    verificarMinas(bloco.l, bloco.c);
                            }
                        }
                        else if (e.getButton() == MouseEvent.BUTTON3){
                            // se não tem ícone -> adiciona a bandeira
                            if(bloco.getIcon() == null && bloco.isEnabled()){
                                if(bandeirasSobrando > 0) {
                                    bloco.setIcon(iconeBandeira);
                                    bloco.setText("");
                                    bandeirasSobrando--;
                                    textoTitulo.setText("Bandeiras disponíveis: " + Integer.toString(bandeirasSobrando));
                                }
                            } else if (bloco.getIcon() == iconeBandeira) { // se ja tem uma bandeira
                                bloco.setIcon(null);
                                bandeirasSobrando++;
                                textoTitulo.setText("Bandeiras disponíveis: " + Integer.toString(bandeirasSobrando));
                            }
                        }
                    }
                });
                painelTabuleiro.add(bloco);

            }
        }

        // Listener para redimensionar as imagens dinamicamente
        painelTabuleiro.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newWidth = painelTabuleiro.getWidth();
                int newHeight = painelTabuleiro.getHeight();

                int newBlockSize = Math.min(newWidth / numColunas, newHeight / numLinhas);

                int newFontSize = (int) (newBlockSize * 0.6);

                for (int l = 0; l < numLinhas; l++) {
                    for (int c = 0; c < numColunas; c++) {
                        tabuleiro[l][c].setFont(new Font("Arial Unicode MS", Font.PLAIN, newFontSize));

                        // Redimensiona a bomba ou bandeira se elas existirem
                        if (tabuleiro[l][c].getIcon() != null) {
                            ImageIcon currentIcon = (ImageIcon) tabuleiro[l][c].getIcon();
                            Image img = currentIcon.getImage();
                            Image scaledImg = img.getScaledInstance(newBlockSize - 20, newBlockSize - 20, Image.SCALE_SMOOTH);
                            tabuleiro[l][c].setIcon(new ImageIcon(scaledImg));
                        }
                    }
                }
            }
        });

        botaoReiniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reiniciarJogo();
            }
        });
        botaoReiniciar.setBackground(new Color(230, 230, 230));

        frame.setVisible(true);

        plantarMinas();
    }

    void reiniciarJogo() {
        gameOver = false;
        blocosClicados = 0;
        minutos = 0;
        segundos = 0;
        textoCronometro.setText("00:00");
        if (cronometro != null) {
            cronometro.stop();
        }

        for (int l = 0; l < numLinhas; l++) {
            for (int c = 0; c < numColunas; c++) {
                Blocos bloco = tabuleiro[l][c];
                bloco.setEnabled(true);
                bloco.setText("");
                bloco.setIcon(null); // Limpa o ícone também
            }
        }
        plantarMinas();
    }

    void plantarMinas(){
        listaMinas = new ArrayList<Blocos>();
        int minasSobrando = quantMinas;
        textoTitulo.setText("Bandeiras disponíveis: " + Integer.toString(minasSobrando));

        while (minasSobrando > 0){
            int l = aleatorio.nextInt(numLinhas);
            int c = aleatorio.nextInt(numColunas);

            Blocos bloco = tabuleiro[l][c];
            if(!listaMinas.contains(bloco)){
                listaMinas.add(bloco);
                minasSobrando -= 1;
            }
        }
    }

    void revelarMinas(){
        for (int i=0; i < listaMinas.size(); i++){
            Blocos bloco = listaMinas.get(i);
            bloco.setIcon(iconeBomba); // Usa o ícone da bomba
        }

        gameOver = true;
        textoTitulo.setText("Você explodiu.");
        cronometro.stop();
    }

    void verificarMinas(int l, int c){

        if (l < 0 || l >= numLinhas || c < 0 || c >= numColunas){
            return;
        }

        Blocos blocos = tabuleiro[l][c];
        if(!blocos.isEnabled()){
            return;
        }

        if(blocosClicados == 0){
            iniciarCronometro();
        }

        blocos.setEnabled(false);
        blocos.setIcon(null); // Remove a bandeira se houver
        blocosClicados+=1;

        int minasEncontradas = 0;

        minasEncontradas += contarMinas(l - 1, c - 1);
        minasEncontradas += contarMinas(l - 1, c);
        minasEncontradas += contarMinas(l - 1, c + 1);
        minasEncontradas += contarMinas(l, c - 1);
        minasEncontradas += contarMinas(l, c + 1);
        minasEncontradas += contarMinas(l + 1, c - 1);
        minasEncontradas += contarMinas(l + 1, c);
        minasEncontradas += contarMinas(l + 1, c + 1);

        if (minasEncontradas > 0){
            blocos.setText(Integer.toString(minasEncontradas));
        } else{
            blocos.setText("");

            verificarMinas(l - 1, c - 1);
            verificarMinas(l - 1, c);
            verificarMinas(l - 1, c + 1);
            verificarMinas(l, c - 1);
            verificarMinas(l, c + 1);
            verificarMinas(l + 1, c - 1);
            verificarMinas(l + 1, c);
            verificarMinas(l + 1, c + 1);

        }

        if(blocosClicados == numLinhas * numColunas - listaMinas.size()){
            gameOver = true;
            textoTitulo.setText("Você venceu!");
            cronometro.stop();
            // dá erro no console, mas continua funcionando
        }
    }

    int contarMinas(int l, int c){
        if (l < 0 || l >= numLinhas || c < 0 || c >= numColunas){
            return 0;
        }
        if(listaMinas.contains(tabuleiro[l][c])){
            return 1;
        }
        return 0;
    }
}