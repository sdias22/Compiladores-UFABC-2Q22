package ArmazenaVariaveis;

import java.util.ArrayList;
import java.util.HashMap;

public class IsiVarTable {
	
	private HashMap<String, IsiVariable> map;
	
	public IsiVarTable() {
		map = new HashMap<String, IsiVariable>();
	}
	
	public void add(IsiVariable variable) {
		map.put(variable.getName(), variable);
	}
	
	public boolean exists(String variableName) {
		return map.containsKey(variableName);
	}
	
	public IsiVariable get(String variableName) {
		return map.get(variableName);
	}
	
	public ArrayList<IsiVariable> getAll(){
		ArrayList<IsiVariable> lista = new ArrayList<IsiVariable>();
		for (IsiVariable variable : map.values()) {
			lista.add(variable);
		}
		return lista;
	}

}