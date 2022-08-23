package ast;

public abstract class AbstractCommand {
    public abstract void executa();
	public abstract String generateJavaCode();
    public abstract String generateCCode();
}