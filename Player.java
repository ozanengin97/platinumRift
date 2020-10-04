import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

// Juan Carlos

class Player {
	static TreeMap<Integer,Hexagono> hexagonos = new TreeMap<Integer,Hexagono>();
	static ArrayList<Escuadron> escuadrones = new ArrayList<>();
	
	public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int playerCount = in.nextInt(); // the amount of players (always 2)
        int myId = in.nextInt(); // my player ID (0 or 1)
        int zoneCount = in.nextInt(); // the amount of zones on the map
        int linkCount = in.nextInt(); // the amount of links between all zones
        
        for (int i = 0; i < zoneCount; i++) {
            int zoneId = in.nextInt(); // this zone's ID (between 0 and zoneCount-1)
            int platinumSource = in.nextInt(); // Because of the fog, will always be 0
           
            Hexagono zona = new Hexagono(zoneId);
            hexagonos.put(zoneId, zona);
        }
        
        for (int i = 0; i < linkCount; i++) {
            int zone1 = in.nextInt();
            int zone2 = in.nextInt();
            
            Hexagono zona1 = hexagonos.get(zone1);
            Hexagono zona2 = hexagonos.get(zone2);
            
            zona1.addColindate(zona2);
            zona2.addColindate(zona1);
            
            hexagonos.replace(zone1, zona1);
            hexagonos.replace(zone2, zona2);
        }
        
        
        int turno = 0;
        Hexagono baseEnemiga = null;
        Hexagono basePropia = null;
        Hexagono basePropiaAux = null;
        
        // game loop
        while (true) {
        	turno++;
            int myPlatinum = in.nextInt(); // your available Platinum
            for (int i = 0; i < zoneCount; i++) {
                int zId = in.nextInt(); // this zone's ID
                int ownerId = in.nextInt(); // the player who owns this zone (-1 otherwise)
                int podsP0 = in.nextInt(); // player 0's PODs on this zone
                int podsP1 = in.nextInt(); // player 1's PODs on this zone
                int visible = in.nextInt(); // 1 if one of your units can see this tile, else 0
                int platinum = in.nextInt(); // the amount of Platinum this zone can provide (0 if hidden by fog)
                
                Hexagono zona = hexagonos.get(zId);
                
                if(ownerId==myId){
                	zona.setOwned(true);
                }else{
                	zona.setOwned(false);
                }
                
                zona.setPlatino(platinum);
                zona.setVisible(visible==1?true:false);
                
                if(myId==0){
                	zona.setPODmios(podsP0);
                	zona.setPODenemigo(podsP1);
                }else{
                	zona.setPODmios(podsP1);
                	zona.setPODenemigo(podsP0);
                }
                
                // guardo la base donde se encuentran los PODs, tanto los míos como los del enemigo, en el turno 1.
                if(turno==1 && (podsP0!=0 || podsP1!=0)){
                	boolean miBaseEncontrada = false;
                	if(myId==0 && podsP0!=0){
                		basePropia = zona;
                		basePropiaAux = zona;
                	}else if(myId==0 && podsP1!=0){
                		baseEnemiga = zona;
                	}else if(myId==1 && podsP0!=0){
                		baseEnemiga = zona;
                	}else{
                		basePropia = zona;
                		basePropiaAux = zona;
                	}
                }

                
                hexagonos.replace(zId, zona);
                	
            }


/*
            // comprobamos los PODs míos y del enemigo
            int numPODmios = basePropia.getPODmios();
            int numPODenemigo = getPODenemigo();
            
            if(numPODmios==0){ // si los PODs de mi Hexagono son 0 llamo a otros ubicados en la basePropia
            	basePropia = hexagonos.get(new Integer(basePropiaAux.getId()));
            	numPODmios = basePropia.getPODmios();
            }
            
            // traigo el Hexagono colindate a visitar
            Hexagono hexaColi = hexagonos.get(new Integer(basePropia.getId())).getColindante();
            
            // comrpuebo que el Hexagono traído no sea la base del enemigo
            boolean esBaseEnemiga = true;
            
            do{
            	
            	if(hexaColi.equals(baseEnemiga)){
            		esBaseEnemiga = true;
            	}else{
            		esBaseEnemiga = false;
            	}
            	
            }while(esBaseEnemiga);
*/           
            // si en la basePropia dispongo de 10 PODs creo un escuadrón
            if(basePropiaAux.getPODmios()>=5){
            	Escuadron escuadron = new Escuadron(5);
            	escuadron.setPrimerHexagono(basePropiaAux); // punto de inicio la basePropiaAux
            	escuadrones.add(escuadron);
            }
            
            String movimientos = "";
            
            // 1º eliminamos los escuadrones que se han quedado sin efectivos
            // 2º movemos todos los escuadrones activos
            
            // 1º
            Iterator<Escuadron> iterador = escuadrones.iterator();
            while(iterador.hasNext()){
            	Escuadron escuadra = iterador.next();
            	Hexagono ultimo = escuadra.getLastHexagono();
            	Hexagono ultimoHexActualizado = hexagonos.get(ultimo.getId());
            	if(ultimoHexActualizado.getPODmios()==0){
            		// no hay PODs en esta ubicación, han sido destruidos. Eliminar escuadra
            		iterador.remove();
            	}
            }
            
            // 2º
            int indice = 0;
            for(Escuadron escuadra : escuadrones){
            	Hexagono ultimo = escuadra.getLastHexagono();
            	Hexagono ultimoHexActualizado =  hexagonos.get(ultimo.getId());
            	int PODs = (ultimoHexActualizado.getPODmios()>4 ? 5 : ultimoHexActualizado.getPODmios());
            	Hexagono colindante = ultimoHexActualizado.getColindante();
            	int retroceder = 0;
        		if(colindante!=null){
        			movimientos += PODs + " " + ultimoHexActualizado.getId() + " " + colindante.getId() + " ";
        			escuadra.avanzarA(colindante);
        		}else{
        			// todos los colindates son nuestros
        			// retrocedemos hasta posición guardada colindante
        			Hexagono colinda = null;
        			boolean seguirBuscando = true;
        			do{
        				retroceder++;
        				colinda = escuadra.retroceder(retroceder);
        				if(colinda==null){
        					seguirBuscando = false;
        				}else if(ultimoHexActualizado.esColindate(colinda)){
        					seguirBuscando = false;
        				}
        			}while(seguirBuscando);
        			
        			if(colinda!=null){

        				movimientos += PODs + " " + ultimoHexActualizado.getId() + " " + colinda.getId() + " ";
        				
        			}else{
        				
        				colinda = ultimoHexActualizado.getColindanteOwned();   
        				escuadra.avanzarA(colinda);
        				movimientos += PODs + " " + ultimoHexActualizado.getId() + " " + colinda.getId() + " ";
        			}
        		}
        		
        		escuadrones.set(indice, escuadra);
        		indice++;
            }
            
          //System.out.println((numPODmios-5) + " " + basePropia.getId() + " " + hexagonos.firstKey() + " " + (numPODmios-5) + " " + basePropia.getId() + " " + hexagonos.lastKey());
          //System.out.println(numPODmios + " " + basePropia.getId() + " " + hexaColi.getId());
            System.out.println(movimientos);
            System.out.println("WAIT"); // no borrar
            

        }
        
        
    }
	
	static int getPODmios(){
		int numPOD = 0;
		for(Hexagono hexagono: hexagonos.values()){
			numPOD += hexagono.getPODmios();
		}
		return numPOD;
	}
	
	static int getPODenemigo(){
		int numPOD = 0;
		for(Hexagono hexagono: hexagonos.values()){
			numPOD += hexagono.getPODenemigo();
		}
		return numPOD;
	}
	
}

class Hexagono{
	private int id;
	private boolean owned;
	private ArrayList<Hexagono> colindantes;
	private int platino;
	private boolean visible;
	private int PODmios;
	private int PODenemigo;
	private String ownedBy;
	
	public Hexagono(int id){
		this.id = id;
		colindantes = new ArrayList<Hexagono>();
		this.owned = false;
		this.platino = 0;
		this.visible = false;
		PODmios = 0;
		PODenemigo = 0;
	}
	
	public Hexagono getColindanteOwned() {
		/*
		for(Hexagono hex: colindantes){
			if(hex.getOwned())
				return hex;
		}
		*/
		return colindantes.get(new Random().nextInt(colindantes.size()));
	}

	public boolean esColindate(Hexagono hex){
		int idHex = hex.getId();
		for(Hexagono colindate: colindantes){
			if(colindate.getId()==idHex)
				return true; // son Hexagonos colindantes
		}
		return false;
	}
	
	public void addColindate(Hexagono hexagono){
		colindantes.add(hexagono);
	}
	
	public void setOwned(boolean mia){
		this.owned = mia;
	}
	
	public void setPlatino(int platino){
		this.platino = platino;
	}
	
	public void setVisible(boolean visibilidad){
		this.visible = visibilidad;
	}
	
	public void setPODmios(int num){
		this.PODmios = num;
	}
	
	public void setPODenemigo(int num){
		this.PODenemigo = num;
	}
	
	public int getPODmios(){
		return PODmios;
	}
	
	public int getPODenemigo(){
		return PODenemigo;
	}
	
	public int getId(){
		return id;
	}
	
	public boolean getOwned(){
		return owned;
	}
	
	public Hexagono getColindante(){
		for(Hexagono hex: colindantes){
			if(!hex.getOwned())
				return hex;
		}
		//return colindantes.get(new Random().nextInt(colindantes.size()));
		return null; // todos los hexagonos colindates son nuestros
	}
}

class Escuadron{
	
	private int numEfectivos;
	boolean avanzando;
	TreeMap<Integer, Hexagono> zonaRecorrida;
	Hexagono lastHexagono;
	private int posRetrocedidas;
	
	public Escuadron(int efectivos){
		numEfectivos = efectivos;
		avanzando = false;
		zonaRecorrida = new TreeMap<Integer, Hexagono>();
		lastHexagono = null;
		posRetrocedidas = 0;
	}
	
	public boolean avanzarA(Hexagono hexagono){
		lastHexagono = hexagono;
		zonaRecorrida.put(zonaRecorrida.size()+1, lastHexagono);
		avanzando = true;
		return true;
	}
	
	public Hexagono retroceder(int posicion){
		int dim = zonaRecorrida.size();
		posRetrocedidas++;
		if(dim<2){
			lastHexagono = zonaRecorrida.get(0);
			avanzando = false;
			return null; // todavía no ha avanzado el escuadrón
		}
		Hexagono hexa = null;
		if(posRetrocedidas<=dim){
			hexa = zonaRecorrida.get(dim-posRetrocedidas);
		}else{
			hexa = zonaRecorrida.get(0);
			posRetrocedidas = 0;
		}
		avanzando = false;
		lastHexagono = hexa;
		return hexa;
	}
	
	public Hexagono retroceder(){
		// --------------- implementar
		return null;
	}
	
	public Hexagono getLastHexagono(){
		return lastHexagono;
	}
	
	public boolean retrocediendo(){
		if(avanzando)
			return false;
		else
			return true;
	}
	
	public boolean isAvanzando(){
		return avanzando;
	}
	
	public void setPrimerHexagono(Hexagono hex){
		this.lastHexagono = hex;
		zonaRecorrida.put(0, hex); // punto inicial del recorrido (es la base de los PODs-efectivos)
	}

}
