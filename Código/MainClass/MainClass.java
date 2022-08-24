import Parser.*;
import Exceptions.*;

public class MainClass {
    public static void main(String[] args) {
        try{
            IsiScanner  sc  = new   IsiScanner("input.isi");
            IsiParser   pa  = new   IsiParser(sc);

            pa.prog();
            pa.varSemUso();
            System.out.println("Compilação Bem Sucedida!");

            pa.geraCodJava();
            System.out.println("Código Java gerado!");

            pa.geraCodC();
            System.out.println("Código C gerado!");
            
        }
        catch (IsiLexicalException ex) {
			System.out.println("Erro léxico! " +ex.getMessage());
		}
		catch (IsiSyntaxException ex) {
			System.out.println("Erro sintético! " +ex.getMessage());
		}
        catch (IsiSemanticException ex){
            System.out.println("Erro semântico! " +ex.getMessage());
        }
		catch (Exception ex) {
			System.out.println("Erro genérico! " +ex.getClass().getName());
		}
    }
}