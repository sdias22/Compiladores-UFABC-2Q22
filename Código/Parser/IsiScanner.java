package Parser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Exceptions.*;

public class IsiScanner {
    // codigo = Vetor que armazena o código
    private char[] codigo;
    // estado = guarda qual o estado está para classificar o tipo e a informação do Token
	private int    estado;
    // pos = Posição no vetor código.
	private int    pos;
    // lin = Linha do código que se está lendo, usada para identificar onde está o erro.
	private int    lin;
    // col = Coluna do código que se está lendo, usada para identificar onde está o erro.
	private int    col;
	
    // Lê o arquivo e coloca em um array
	public IsiScanner(String arquivo) {
		try {
			lin = 1;
			col = 0;
			String txtConteudo;
			txtConteudo = new String(Files.readAllBytes(Paths.get(arquivo)),StandardCharsets.UTF_8);

			codigo = txtConteudo.toCharArray();
			pos=0;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

    // Palavras Reservadas
    private final List <String> TK_PReserv = new ArrayList<>(Arrays.asList("programa", "fimprog", "declare", "escreva", "leia", "se", "entao", "senao", "fimse", "enquanto", "faca", "fimenquanto", "escolha", "caso", "fimcaso", "semcaso", "fimescolha", "inteiro", "pontof" , "texto"));
    
    public Token nextToken() {
		char posAtual;
		Token token;
		String termo="";
		if (isEOF()) {
			return null;
		}
		estado = 0;
		while (true) {
			posAtual = nextChar();
			col++;
            switch (estado) {
                case 0:
                    // Entra se for um operador aritmético
                    if (ehOpArit(posAtual)) {
                        termo += posAtual;
                        token = new Token();
                        token.setType(Token.TK_OpArit);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    // Entra se for um '('
                    } else if (ehAP(posAtual)) {
                        termo += posAtual;
                        token = new Token();
                        token.setType(Token.TK_AbreParentes);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    // Entra se  for um ")"
                    } else if (ehFP(posAtual)) {
                        termo += posAtual;
                        token = new Token();
                        token.setType(Token.TK_FechaParentes);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    // Entra se for um '.', que representa o fim da instrução
                    } else if (ehFimLinha(posAtual)) {
                        termo += posAtual;
                        token = new Token();
                        token.setType(Token.TK_Ponto);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    // Entra se for virgula, significa separador de IDs
                    } else if (ehVirgula(posAtual)){
                        termo += posAtual;
                        token = new Token();
                        token.setType(Token.TK_Virgula);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    // Entra se for um espaço
                    } else if (ehEspaco(posAtual)){
                        if(posAtual == '\n'){
                            lin++;
                            col = 0;
                        }
                    // Entra se for o inicio de um texto
                    }else if (ehAspas(posAtual)) {
                        termo += posAtual;
                        estado = 1;
                    // Entra se for o inicio de um número
                    } else if (ehDigito(posAtual)) {
                        termo += posAtual;
                        estado = 3;
                    // Entra se for o inicio de um identificador (Variável ou Palavra Reservada)
                    } else if (ehCaract(posAtual)) {
                        termo += posAtual;
                        estado = 5;
                    // Entra se for o inicio do operador relacional "=="
                    } else if (ehIgual(posAtual)) {
                        termo += posAtual;
                        estado = 6;
                    // Entra se for um operador relacional ou ínicio de um ">=" ou "<="
                    } else if (ehOpRel(posAtual)){
                        termo += posAtual;
                        estado = 7;
                    } else if (ehAtrib(posAtual)){
                        termo += posAtual;
                        estado = 8;
                    } else {
                        throw new IsiLexicalException("Simbolo não reconhecido! Linha: " + lin + " Coluna: " + col);
                    }
                    break;
            
                // Verifica se é um texto, ter pelo menos um caracter ou digito ou espaço.
                case 1:
                    if (ehCaract(posAtual) || ehDigito(posAtual) || ehEspaco(posAtual) || ehAP(posAtual) || ehAtrib(posAtual) || ehFP(posAtual) || ehFimLinha(posAtual) || ehOpArit(posAtual) || ehIgual(posAtual) || ehOpRel(posAtual) || ehVirgula(posAtual)) {
                        estado = 2;
                        termo += posAtual;
                    }else{
                        throw new IsiLexicalException("Texto não reconhecido! Linha: " + lin + " Coluna: " + col);
                    }
                    break;
                // Verifica se é um texto.
                case 2:
                    if (ehCaract(posAtual) || ehDigito(posAtual) || ehEspaco(posAtual) || ehAP(posAtual) || ehAtrib(posAtual) || ehFP(posAtual) || ehFimLinha(posAtual) || ehOpArit(posAtual) || ehIgual(posAtual) || ehOpRel(posAtual) || ehVirgula(posAtual)) {
                        estado = 2;
                        termo += posAtual;
                    }else if(ehAspas(posAtual)){
                        termo += posAtual;
                        token = new Token();
                        token.setType(Token.TK_Texto);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    }else{
                        throw new IsiLexicalException("Texto não reconhecido! Linha: " + lin + " Coluna: " + col);
                    }
                    break;
                // Verifica se é número.
                case 3:
                    if(ehDigito(posAtual)){ 
                        estado = 3;
                        termo += posAtual;
                    }else if(ehVirgula(posAtual)){
                        estado = 4;
                        termo += posAtual;
                    }else if (ehAtrib(posAtual) || ehEspaco(posAtual) || ehOpArit(posAtual) || ehOpRel(posAtual) || ehIgual(posAtual) || ehFP(posAtual) || ehFimLinha(posAtual)) {
                        if (!isEOF(posAtual))
						    back();
                        token = new Token();
                        token.setType(Token.TK_IntNumero);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    }else {
                        throw new IsiLexicalException("Número não reconhecido! Linha: " + lin + " Coluna: " + col);
                    }
                    break;
                // Verifica se é número com vírgula.
                case 4:
                    if(ehDigito(posAtual)){ 
                        estado = 4;
                        termo += posAtual;
                    }else if (ehAtrib(posAtual) || ehEspaco(posAtual) || ehOpArit(posAtual) || ehOpRel(posAtual) || ehIgual(posAtual) || ehFP(posAtual) || ehFimLinha(posAtual)) {
                        if (!isEOF(posAtual))
						    back();
                        token = new Token();
                        token.setType(Token.TK_DoubleNumero);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    }else {
                        throw new IsiLexicalException("Número não reconhecido! Linha: " + lin + " Coluna: " + col);
                    }
                    break;
                // Verifica se é um ID, que pode ser uma palavra reservada ou uma variável.
                case 5:
                    if(ehCaract(posAtual) || ehDigito(posAtual)) {
                        estado = 5;
                        termo += posAtual;
                    } else if(ehAtrib(posAtual) || ehEspaco(posAtual) || ehOpArit(posAtual) || ehOpRel(posAtual) || ehIgual(posAtual) || ehFP(posAtual) || ehAP(posAtual) || ehVirgula(posAtual) || ehFimLinha(posAtual)){
                        if (!isEOF(posAtual))
						    back();
                        token = new Token();
                        if(TK_PReserv.contains(termo.toLowerCase())){
                            token.setType(Token.TK_PResarvada);
                        }else{
                            token.setType(Token.TK_Variavel);
                        }
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    } else{
                        throw new IsiLexicalException("ID não reconhecido! Linha: " + lin + " Coluna: " + col);
                    }
                    break;
                // Verifica se é o operador relacional '=='   
                case 6:
                    if(ehIgual(posAtual)){
                        termo += posAtual;
                        token = new Token();
                        token.setType(Token.TK_OpRel);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    } else{
                        throw new IsiLexicalException("Operador Relacional não reconhecido! Linha: " + lin + " Coluna: " + col);
                    }
                    //break;
                // Verifica se é um operador relacional
                case 7:
                    if(ehIgual(posAtual)){
                        termo += posAtual;
                        token = new Token();
                        token.setType(Token.TK_OpRel);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    }
                    if(ehEspaco(posAtual) || ehCaract(posAtual) || ehDigito(posAtual) || ehFimLinha(posAtual)){
                        if (!isEOF(posAtual))
						    back();
                        token = new Token();
                        token.setType(Token.TK_OpRel);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    } else{
                        throw new IsiLexicalException("Operador Relacional não reconhecido! Linha: " + lin + " Coluna: " + col);
                    }
                    //break;
                // Verifica se é um operador de atribuição
                case 8:
                    if (ehIgual(posAtual)){
                        termo += posAtual;
                        token = new Token();
                        token.setType(Token.TK_OpAtrib);
                        token.setText(termo);
                        token.setLine(lin);
                        token.setColumn(col - termo.length());
                        return token;
                    }else{
                        throw new IsiLexicalException("Operador de Atribuição não reconhecido (:=)! Linha: " + lin + " Coluna: " + col);
                    }
                    //break;
                default:
                    throw new IsiSyntaxException("Simbolo não reconhecido! Linha: " + lin + " Coluna: " + col);
            }
        }
    }

    // Retorna se é Aspas
    private boolean ehAspas (char c){
        return c == '"';
    }

    // Retorna se é o sinal de igual
    private boolean ehIgual (char c){
        return c == '=';
    }

    // Retorna se é um abre parênteses "("
    private boolean ehAP (char c){
        return c == '(';
    }
    
    // Retorna se é um fecha parênteses ")"
    private boolean ehFP (char c){
        return c == ')';
    }

    // Retorna se é um caracter
    private boolean ehDigito (char c){
        return c>= '0' && c <= '9';
    }

    // Retorna se é um caracter
    private boolean ehCaract (char c){
        return (c >= 'a' && c <= 'z') || (c>='A' && c <= 'Z');
    }

    // Retorna se é um operador aritmético
    private boolean ehOpArit (char c) {
		return c == '+' || c == '-' || c == '*' || c == '/';
	}

    // Retorna se é um operador relacional
    private boolean ehOpRel (char c){
        return c == '>' || c == '<' || c == '!';
    }

    // Retorna se é um espaço
	private boolean ehEspaco (char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r'; 
	}

    // Retorna se é um ponto final que encerra a linha / instrução
    private boolean ehFimLinha (char c){
        return c == '.';
    }

    // Retorna se é uma atribuição
    private boolean ehAtrib (char c){
        return c == ':';
    }

    // Retorna se é um separador
    private boolean ehVirgula (char c){
        return c == ',';
    }

    // Retorna o próximo caracter do código
    private char nextChar() {
		if (isEOF()) {
			return '\0';
		}
		return codigo[pos++];
	}

    // Retorna se o código acabou
    private boolean isEOF() {
		return pos >= codigo.length;
	}

    // Retorna se o código acabou
    private boolean isEOF(char c) {
    	return c == '\0';
    }

    // Volta uma posição no vetor com o código
    private void back() {
    	pos--;
    	col--;
    }
}