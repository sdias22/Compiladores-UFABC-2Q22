package Parser;

public class Token {
	public static final int TK_PResarvada       = 0;
	public static final int TK_Variavel			= 1;
    public static final int TK_Texto            = 2;
	public static final int TK_IntNumero        = 3;
	public static final int TK_DoubleNumero     = 4;
	public static final int TK_OpRel            = 5;
    public static final int TK_OpArit           = 6;
	public static final int TK_Ponto            = 7;
	public static final int TK_OpAtrib          = 8;
    public static final int TK_AbreParentes     = 9;
    public static final int TK_FechaParentes    = 10;
	public static final int TK_Virgula			= 11;

	public static final String TK_TEXT[] = {
		"Palavra Reservada", "Variavel", "Texto", "Número Inteiro", "Número com ponto Flutuante", "Operador Relacional", "Operador Aritimetico", "Ponto", "Operador de Atribuição", "Abre Parenteses", "Fecha Parenteses", "Virgula"
	};
	
	private int    tipo;
	private String texto;
	private int    lin;
	private int    col;
	
	public Token(int tipo, String texto) {
		super();
		this.tipo  = tipo;
		this.texto = texto;
	}

	public Token() {
		super();
	}

	public int getType() {
		return tipo;
	}

	public void setType(int tipo) {
		this.tipo = tipo;
	}

	public String getText() {
		return texto;
	}

	public void setText(String texto) {
		this.texto = texto;
	}

	@Override
	public String toString() {
		return "Token [tipo=" + TK_TEXT[tipo] + ", texto=" + texto + "]";
	}

	public int getLine() {
		return lin;
	}

	public void setLine(int lin) {
		this.lin = lin;
	}

	public int getColumn() {
		return col;
	}

	public void setColumn(int col) {
		this.col = col;
	}
}