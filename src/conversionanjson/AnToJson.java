/*
 * Résumé: Classe lisant le ficher .an et creéant le fichier .json correspondant
 * avec automatiquement le même nom (seul l'extension change.)
 * Le nom du fichier .an à lire est rentré en paramétre, sans son extension .an. 
 * commentaire court et long autorisé!
 * Code: 4 méthodes "static"  (car l'utilisation est ponctuelle et ne nécessite pas d'attribut. On raisonne peut-être plus en impératif qu'en objet.)
 *     *lectureAn   -la  1ère lit le fichier .an, avec en paramétre son nom privé de son extension. Et elle stoque tout le texte dans un String ("contenu") qu'elle retourne.  
 *     *ecritureStringJson  -la 2nd "parse" (ou traite) le texte de "contenu" en paramétre et écrit le futur contenu du fichier .json dans des Strings intermédiaires (variables locales: gene, coop et fleche).
 *          Elle fonctionne donc en 2 étapes: remplit d'abord gene, puis coop et fleche en même temps.
 *          Elle retourne un String (voir si pas plutot un tableau de String mieux? version futur) correpondant à la "concaténation"(moyennant 2-3 lignes en plus) dans l'odre de gene, coopt et fleche.  
 *     *ecritureJson   -la 3ieme crée et écrit le .json avec le String retourné par la méthode 2.
 *     *methode (ouais je sais: c'est très original^^) -la 4ieme appelle les autres méthodes dans l'ordre de manière à exécuter l'opération de conversion en 1 appel.
 *
 *  // peut être plus pratique avec l'utilisation des classes StringBuffer et StringBuilder (plus flexible, on peut modifier la chaine)
 *
 *  A ajouter:           -faire/traiter initial_context dans "VALUE" de gène ( 1= actif, 0=inactif)
 *                                      
 *                       -faire 2 débuggers pour vérifier le .an si ne reconnait pas le fichier .an : -1 dans une autre classe qui regard la struture global du fichier et la présence de commentaire en indiquant la ligne où il se trouve.
 *                                                                              -1 ici qui suit en détails et permet d'indiquer la ligne problèmatique du .an
 *
* ATTENTION LIGNE 256 !!! :  
 */
package conversionanjson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.CORBA.portable.IndirectionException;

/**
 *
 * @author Romain
 */
public class AnToJson {

    private static Logger logger = Logger.getLogger("Logger");

    public static void methode(String nomAN) {
//        ecritureJson(ecritureStringJson(lectureAn(nomAN), nomAN), nomAN);
        ecritureJson(ecritureStringJson(decommenteur(lectureAn(nomAN))), nomAN);
        ecritureHTML(nomAN);
    }

    /**
     * Méthode static destiné à être appeler par la méthode principale "methode"
     * de la classe AnToJson
     *
     * @param nomAN type String, correspondant au nom sans son extension ".an"
     * du fichier .an à lire
     * @return contenu type String, correspondant exactement au texte du fichier
     * .an
     */
    private static String lectureAn(String nomAN) {
        String contenu = null; // null parceque sinon netbeans détecte qu'il n'a potentiellement pas été initialisé....
        String tmp; // pour stocker la ligne courante en même temps qu'on fait le test
        //tmp est initialisé et changer dans la condition du while. Il y a surement mieux à faire.
        try {
            BufferedReader reader = new BufferedReader(new FileReader(nomAN + ".an")); //ouverture du fichier à lire
            logger.info("Ficher " + nomAN + ".an ouvert.");

            contenu = reader.readLine();   // initialisation de remplissseur sur la 1ère ligne //On aurait aussi pu utiliser nomAN puiqu'il est inutile pour la suite de cette méthode
            while ((tmp = reader.readLine()) != null) {  // while pour parcourir chaque ligne qui est stocker dans tmp en même temps que l'on vérifie que ligne non vide
                //On est obliger d'utiliser tmp car le readline fait passer à la ligne suivante.
                contenu += "\n"; //indique le saut de ligne
                contenu += tmp; //on ajoute la nouvelle ligne
            }

            reader.close(); // fermeture du BufferedReader
            logger.info("Fichier .an lu et stoqué dans un String.");
//            System.out.println(remplisseur);  //test de vérification
        } catch (FileNotFoundException e1) {
            System.err.println("Problème d'ouverture du fichier.");
            e1.toString();
        } catch (IOException ex) {
            System.err.println("Erreur de flux dans la lecture de la ligne");
            Logger.getLogger(AnToJson.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//useless
        }
        return contenu;
    }
/**
 * Méthode qui élimine les commentaires court % et long (*...*) des fichiers .an . Toutes autres pollutions du texte conduira à une erreur
 * @param contenu
 * @return 
 */
    public static String decommenteur(String contenu) {
//        System.out.println("test");
        StringBuffer contenub = new StringBuffer(contenu);
        int debut;
        int fin = -1;
        while (contenu.contains("%")) {
            debut = contenub.indexOf("%");

            try {
                fin = contenub.indexOf("\n", debut);
            } catch (java.lang.StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                logger.info("pas de fin de ligne, c'est une fin de doc/string ");
            } finally {
                if (fin == -1) {
                    fin = contenub.length();
                }
            }
            contenub.delete(debut, fin);
            fin = -1;
//            contenu = contenub.toString();
            System.out.println(" commentaire court enlevé");
//            System.out.println(contenu);
        }

        while (contenu.contains("(*")) {
            debut = contenub.indexOf("(*");
            fin = contenub.indexOf("*)", debut);
            contenub.delete(debut, fin + 2);
            contenu = contenub.toString();
            System.out.println(" commentaire long enlevé");
//            System.out.println(contenu);
        }
//        System.out.println("==============\n" + contenu);
        return contenu;
    }

    /**
     * Méthode qui traite le String retourné par "lectureAn", et écrit le
     * fichier ".json".
     *
     * @param remplisseur
     * @type String correspondant au String retourner par la méthode
     * "lectureAn", et donc au texte à traiter
     * @param nomAN
     * @type String , correspondant au nom sans son extension ".an" du fichier
     * .an à lire
     *
     * //Convention: saut de ligne via "\n" (aucun BufferedWriter.newLine();)
     * (but éviter de se perdre dans les mélanges
     *
     * + ,"value":0,"level":0,"gene":"L"}, level donne le numéro de l'état du
     * gène //value =1 si état actif 0 sinon
     *
     * A faire VALUE pour gène avec l'initial context
     *
     * link: value= couleur (0=noir, 1=vert, 2=rouge, 3 flèche sans bout) 
     * faire le détecteur automatique...en javascript plutôt type = 0=flèche droite
     * 1=flèche courbée pour les transitions fait
     *
     */
    public static StringBuffer ecritureStringJson(String remplisseur) {
        int nbLigneEffective=1; // pour le débuggage
        
        StringBuffer gene = new StringBuffer();
        StringBuffer coop = new StringBuffer();
        StringBuffer fleche = new StringBuffer();
        ArrayList<String> l = new ArrayList<String>();  // Indispensable pour la partie flèche. Liste chaque état de chaque géne dans l'ordre. Le numéro de la ligne est indispensable pour bien positionner les flèches

        String delimiteurs = " \n\",[]=";           //initialisation du tokenizer pour parser le string ( \n pour les saut de ligne, sinon considéré comme un mot espace par le tok)
        StringTokenizer tok = new StringTokenizer(remplisseur, delimiteurs);
//============================================================================================================ NODE GENES

        gene.append("{\n" + "  \"nodes\":[\n");  // écriture 2 1ères ligne d'entête ; situation: dans écriture des "nodes" (gènes) 
//writer.write("{\n" + "  \"nodes\":[\n"); 
int indiceLevelCoop = 0;//pour les coopérations voir 2nd partie
try{
        int nGroupe = 1;
        String nomGeneTemporaire = tok.nextToken(); //utilisé pour les différents états du géne.
        String nomPrem = nomGeneTemporaire; // utilisé pour détecter la sortie de la boucle de création des gènes, et rentrer dans la partie "links" 

        String nomProchainGene = null; //voir utilité après //obligé de l'initialiser pour éviter avertissement de netbeans; et permet de rentrer la 1ère fois dans la boucle            
        int etat = 1;
        boolean memeGroupe = true;
        while (!nomPrem.equals(nomProchainGene)) {  // faiblesse de la condition d'arrêt A AMELIORER
            gene.append("    {\"name\":\"" + nomGeneTemporaire + "." + tok.nextToken() + "\",\"group\":" + nGroupe + ",\"value\":0,\"level\":0,\"gene\":\"" + nomGeneTemporaire + "\"},\n"); // + ,"value":0,"level":0,"gene":"L"},
            // {"name":"AKT1.1","group":1},              // level donne le numéro de l'état du gène //value =1 si état actif 0 sinon 
            l.add(nomGeneTemporaire + ".0");   // numérote le moindre état d'un géne dans l'ordre
//writer.write("    {\"name\":\"" + nomGeneTemporaire + "." + tok.nextToken() + "\",\"group\":" + nGroupe + "},\n"); //1ère ligne du groupe suivant //tok.nextToken() vaut toujours 0 ici
//                writer.write(tok.nextToken());
//                writer.write(tok.nextToken());
//                writer.write(tok.nextToken());
nbLigneEffective++;
            memeGroupe = true; // pour re rentrer dans les boucles des états d'un autre géne.
            etat = 1;           // idem avec la bonne valeur initial de etat
            while (memeGroupe) {   // boucle pour les états d'un gène (si plus de 1 état dans le gène

                nomProchainGene = tok.nextToken();       // = numéro de l'état tant qu'il y a un état à rajouter au groupe (ou gène); sinon = au nom du prochain gène à traiter
                //    System.out.println(nomProchainGene);
                if (nomProchainGene.equals(Integer.toString(etat))) {
                    gene.append("    {\"name\":\"" + nomGeneTemporaire + "." + etat + "\",\"group\":" + nGroupe + ",\"value\":0,\"level\":" + etat + ",\"gene\":\"" + nomGeneTemporaire + "\"},\n");
                    l.add(nomGeneTemporaire + "." + etat);   // numérote le moindre état d'un géne dans l'ordre
//writer.write("    {\"name\":\"" + nomGeneTemporaire + "." + etat + "\",\"group\":" + nGroupe + "},\n");
//           System.out.println(tok.nextElement());
//           System.out.println(tok.nextElement());
                    etat++;
                    nbLigneEffective++;
                } else {
                    memeGroupe = false;
                }
//                System.out.println("etat : " + etat);
            }
            memeGroupe = true;

            nGroupe++; // on passe au groupe suivant
            nomGeneTemporaire = nomProchainGene; //Importance de nomProchainGene, qui sera encore utilisé dans le prochain if de la boucle while pour les états.
        }
//        System.out.println("gene: " + gene);  // juste pour tester
//A la sortie, "nomProchainGene" ou "nomGeneTemporaire = au nom du 1er genes avec changement d'état, suite à une frappe en coop ou pas.
//============================================================================================================ NODE COOP et flèche
        fleche.append("  \"links\":[\n"); //écriture des 2 premières lignes pour la partie liens/links du fichier JSON

        //inutil  String nomProchainGene2 = ""; // pour gérer le "and"; nomProchainGene déjà utilisé pour le when dans cette partie. 
        String etatIni = "0";    // état initial origine de la flèche 
        String etatFin = "1";    // état final extrémitée de la flèche 
        int source = 0, target = 0; // pour la transition d'état de geneTemporaire (celui qui subit la frappe)
        int indiceCoop = l.size();  // pour avoir la ligne/indice qui permet de bien diriger les flèches.
        ArrayList<String> nomCoop = new ArrayList<String>(); // liste des génes coopérants pour la frappe changeant l'état de nomGeneTemporaire.
        ArrayList<String> nomEtat = new ArrayList<String>(); // liste des états des gènes de la liste précédente.
        ArrayList<String> listeCoopPrec = new ArrayList<String>(); // liste des coops déjà rentré pour vérifier si exite déjà (ça arrive). Sinon ça rajoute un noeud en plus non utilisé dans le graph
        boolean cooptest = false;
        StringBuffer listeLevelCoop = new StringBuffer("abcdefghijklmnopqrstuvwxyz"); // pour donner le level d'une coop //GROS PROBL7ME SI ON DEPASSE Z!!!!!
        indiceLevelCoop = 0;

        while (!nomProchainGene.equals("initial_context")) {

            etatIni = tok.nextToken();
            if (!tok.nextToken().equals("->")) {
                logger.severe("Manque une flèche \"->\" ou problème ");
            }
            etatFin = tok.nextToken();

            source = nomNumero(l, nomGeneTemporaire + "." + etatIni);  //utilité de la liste l du tout début de la méthode
            target = nomNumero(l, nomGeneTemporaire + "." + etatFin);

            if ((nomProchainGene = tok.nextToken()).equals("when")) {
                //System.out.println("condition ");
                nomCoop.add(tok.nextToken());
                nomEtat.add(tok.nextToken());
                while ((nomProchainGene = tok.nextToken()).equals("and")) {    // pb avec gros fichier
                    nomCoop.add(tok.nextToken());  //on remplit la liste de coopérants...
                    nomEtat.add(tok.nextToken());  //...avec leur état correspondant
                    cooptest = true;
                }
                if (cooptest) {
                    String nomscoop = "";
                    for (int j = 0; j < nomCoop.size(); j++) {
                        nomscoop += nomCoop.get(j) + "." + nomEtat.get(j) + "/";
                    }
                    for (int i = 0; i < listeCoopPrec.size(); i++) {
                        if (listeCoopPrec.get(i).equals(nomscoop)) {
                            nomscoop+="*";
                            System.out.println("TEST RENTR2 DANS IF");
                        }
//                        System.out.println("dans la boucle");
                    }
                    listeCoopPrec.add(nomscoop);
                    
                    coop.append("    {\"name\":\"COOP_" + nomscoop + "\",\"group\":" + nGroupe + ",\"value\":0,\"level\":\"--\",\"gene\":\"COOP_" + nomscoop + "\"},\n");  // -- ou  Problème pour les gros réseaux: + listeLevelCoop.charAt(indiceLevelCoop) + "\",\"gene\":\"COOP_" + nomscoop + "\"},\n");  //création du noeud de coop
nbLigneEffective++;
                    for (int k = 0; k < nomCoop.size(); k++) {   //flèche de chaque coopérant vers le noeud de coop, après la création du noeud de coop

                        fleche.append(" {\"source\":" + nomNumero(l, nomCoop.get(k) + "." + nomEtat.get(k)) + ",\"target\":" + indiceCoop + ",\"value\":3,\"type\":\"0\"},\n");
                    nbLigneEffective++;
                    }

                    fleche.append(" {\"source\":" + indiceCoop + ",\"target\":" + source + ",\"value\":0,\"type\":\"0\"},\n"); // fleche du noeud de coop à l'état initial du géne frappé 
                    nbLigneEffective++;
                    indiceCoop++; // valeur pour la prochaine coop
                    nGroupe++;    // idem 
                    indiceLevelCoop++;

                } else {
                    fleche.append(" {\"source\":" + nomNumero(l, nomCoop.get(0) + "." + nomEtat.get(0)) + ",\"target\":" + source + ",\"value\":0,\"type\":\"0\"},\n");
nbLigneEffective++;
                }
                cooptest = false; //pas oublier pour la prochaine boucle

            }
            nomCoop.clear(); //on remet les listes de coopérant à 0
            nomEtat.clear();

            fleche.append(" {\"source\":" + source + ",\"target\":" + target + ",\"value\":0,\"type\":\"1\"},\n");       // écriture de la fléche de transition/saut d'état d'un gène
            nbLigneEffective++;
            nomGeneTemporaire = nomProchainGene;
//            System.out.println("coop: "+coop+"\n"+"fleche: "+fleche+"\n"); //"gene: "+gene+"\n"+
        } //fin while de fleche. On passe à la situation initiale.

//            while (tok.hasMoreElements()) {
//                writer.write(tok.nextToken()); 
//            }

}
catch(Exception e){
    e.printStackTrace();
    System.out.println("ATTENTION peut être une erreur ou juste absence de \"initial context\"\nNuméro dernière ligne écrite: "+nbLigneEffective);
}
if (indiceLevelCoop>=26) {
        logger.severe("==========ATTENTION: ERREUR: impossible d'utiliser les lettres pour les coopération (plus de 26 pour ce fichier)=================================");
        }

        coop.deleteCharAt(coop.lastIndexOf(","));
        coop.append("  ],\n");
        fleche.deleteCharAt(fleche.lastIndexOf(","));
        fleche.append("  ]\n" + "}");
        StringBuffer remplisseurFinal = gene.append(coop).append(fleche);
        logger.info("Parser et traitement du tokenizer sont finis.");
        return remplisseurFinal;
    }

    /**
     * Méthode final, qui crée et écrit le fichier .json (nomAN.json)
     *
     * @param remplisseur
     * @param nomAN
     */
    public static void ecritureJson(StringBuffer remplisseurFinal, String nomAN) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(nomAN + ".json")); //création du fichier .json ( à remplir)
            logger.info("Ficher " + nomAN + ".json créé, et à remplir.");

            writer.write(remplisseurFinal.toString());   // écriture

            writer.close();  // pas oublié de fermer le flux (serait mieux dans le finally
            logger.info("Ficher " + nomAN + ".json remplit.");
        } catch (IOException e2) {
            System.err.println("Problème d'écriture du nouveau fichier.");
            e2.printStackTrace();
        } finally {
//useless for the moment
        }
    }

    /**
     * TODO pour l'intégration modif surement à faire
     * Permet de créer le .html avec ce JSON correspondant, mais reste à ajouter la ligne aux autres html....A faire quoi
     * @param remplisseurFinal
     * @param nomAN 
     */
     public static void ecritureHTML(String nomAN) {
        try {
         
            BufferedWriter writerHTML = new BufferedWriter(new FileWriter(nomAN + ".html")); //création du fichier .json ( à remplir)
            logger.info("Ficher " + nomAN + ".html créé, et à remplir.");
            
            String texteHTML="<!DOCTYPE html>\n" +
"<meta charset=\"utf-8\">\n" +
"<html>\n" +
"\n" +
"<head>\n" +
"    <title>Réseau de Régulation Biologique</title>\n" +
"    \n" +
"    <!--Scripts .js des bibliothèques utilisées - jQuery et D3-->\n" +
"    <script src=\"d3.v2.js\"></script>\n" +
"    <script type=\"text/javascript\" src=\"jquery-1.10.2.js\"></script>\n" +
"    <script type='text/javascript' src=\"jquery-ui.js\"></script>\n" +
"    \n" +
"    <!--Fichier .css de style de toutes les éléments visuels de l'interface-->\n" +
"    <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">\n" +
"    \n" +
"    <center>\n" +
"        <h1><i>Réseau de Régulation Biologique - MeForBio</i></h1> Carlos Eduardo <b>GRIVOL JÚNIOR</b> / Romain <b>GIUDICE</b>\n" +
"        <br>\n" +
"        <br>\n" +
"        \n" +
"        <!--Bouton Sélection de Réseau-->\n" +
"        <select name=\"forma\" onchange=\"location = this.options[this.selectedIndex].value;\">\n" +
"  		<option value=\"index.html\">Circadian</option>\n" +
" 		<option value=\"circlock.html\" selected>Circlock</option>\n" +
"		<option value=\"circlock2.html\">Circlock 2</option>\n" +
"	        <option value=\"metazoan.html\">metazoan</option>\n" +
"		<option value=\"tcrsig94.html\">tcrsig94</option>\n" +
"		<option value=\"ERBB_G1-S.html\">ERBB_G1-S</option>\n" +
"		<option value=\"tcrsig40.html\" >tcrsig40</option>\n" +
"		<option value=\"egfr104.html\">egfr104</option>\n" +
"		<option value=\""+nomAN+".html\">"+nomAN+"</option>\n" +                          //ligne ajouté //dans la version web il faudra téléchager le dernier .html pour avoir toutes les <option> et ensuite en ajouté une ... gestion pour le prochain groupe...dépend de la méthode d'intégration de l'application
"		</select>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp\n" +
"        \n" +
"        <!--Bouton Aide-->\n" +
"        <button onclick=\"aide()\">Aide</button>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp\n" +
"        \n" +
"        <!--Bouton Ouvrir/Fermer-->\n" +
"        <button onclick=\"clicks()\">Ouvrir/Fermer</button>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp\n" +
"        \n" +
"        <!--Bouton Printer-->\n" +
"        <a href=\"whatever.htm\" onClick=\"window.print();return false\"><img src=\"printer.png\"></a>\n" +
"        <br>\n" +
"        <br>\n" +
"        \n" +
"        <!--Bouton Chercher Gène-->\n" +
"        <div class=\"ui-widget\">\n" +
"            <label for=\"search\">Gène: </label>\n" +
"            <select id=\"search\"></select>\n" +
"            <button type=\"button\" onclick=\"searchNode()\">Chercher</button>\n" +
"        </div>\n" +
"        <br>   \n" +
"    </center>\n" +
"</head>\n" +
"\n" +
"<body>\n" +
"    <script type=\"text/javascript\">\n" +
"    	//Script principal - SVG\n" +
"    	//Propriétés du SVG\n" +
"        var width = 960, // largeur svg\n" +
"            height = 600, // hauteur svg\n" +
"            dr = 12, // rayon des noeuds\n" +
"            off = 16, // offset des englobements\n" +
"            expand = {}, // clusters\n" +
"            data, net, force, hullg, hull, linkg, link, nodeg, node; //variables\n" +
"\n" +
"        var curve = d3.svg.line()\n" +
"            .interpolate(\"cardinal-closed\")\n" +
"            .tension(.95); //Changer le format des englobements\n" +
"\n" +
"		//Palette de couleurs de tous les noeuds\n" +
"        var fill = d3.scale.category10();\n" +
"\n" +
"		//Fontion jQuery pour clicker dans tous les éléments d'un type spécifié\n" +
"		//Utilisé pour garantir que tous les gènes soient ouverts au départ!\n" +
"        jQuery.fn.d3Click = function() {\n" +
"            this.each(function(i, e) {\n" +
"                var evt = document.createEvent(\"MouseEvents\");\n" +
"                evt.initMouseEvent(\"click\", true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);\n" +
"                e.dispatchEvent(evt);\n" +
"            });\n" +
"        };\n" +
"\n" +
"		//Fonction toujous retour faux\n" +
"        function noop() {\n" +
"            return false;\n" +
"        }\n" +
"\n" +
"		//initialiser le compteur d'entrés dans la fonction init()\n" +
"        var nbclicks = 0;\n" +
"\n" +
"		//Fonction clicks qui sert laisser tous les gènes ouverts/fermés\n" +
"        function clicks() {\n" +
"            if (nbclicks == 0) {\n" +
"                $(\"path.hull\").d3Click();\n" +
"                nbclicks = 1;\n" +
"            } else {\n" +
"                $(\"circle\").d3Click();\n" +
"                nbclicks = 0;\n" +
"            }\n" +
"        }\n" +
"\n" +
"		//Pop-up d'aide\n" +
"        function aide() {\n" +
"            alert(\"Noeuds -> Tapez Shift pour activer et Ctrl pour désactiver avant de passer la souris sur le noeud \\n \\n Liens -> Tapez Shift pour rouge, Alt pour vert et Ctrl pour noir avant de passer la souris sur le lien \\n \\n Bouton Ouvrir/Fermer -> Appuyez pour voire l'intérieur de tous les gènes ou pour cacher les niveaux des gènes \\n \\n Chercher-> Choisissez le gène souhaité pour le trouver facilement \\n \\n\");\n" +
"            alert(\"Zoom -> Il est possible de zoomer et dézoomer à n'importe quel niveau avec le bouton central de la souris si elle est bien placé sur un noeud \\n \\n Traînée -> Vous pouvez déplacer librement les noeuds et ils seront fixés après les lâcher \\n \\n Clic -> Un clic sur un gène lui fait démonter et un clic sur un niveau du gène réalise l'assemblage de tous les niveaux du gène \\n \\n\");\n" +
"        }\n" +
"\n" +
"		//Fonctions de retour d'id\n" +
"		\n" +
"        function nodeid(n) {\n" +
"            return n.size ? \"_g_\" + n.group : n.name;\n" +
"        }\n" +
"\n" +
"        function linkid(l) {\n" +
"            var u = nodeid(l.source),\n" +
"                v = nodeid(l.target);\n" +
"            return u + \"|\" + v;\n" +
"        }\n" +
"\n" +
"		//Fonction pour obtenir le gène d'un niveau\n" +
"        function getGroup(n) {\n" +
"            return n.group;\n" +
"        }\n" +
"\n" +
"        // Construction du réseau à visualiser\n" +
"        function network(data, prev, index, expand) {\n" +
"            expand = expand || {};\n" +
"            var gm = {}, // group map\n" +
"                nm = {}, // node map\n" +
"                lm = {}, // link map\n" +
"                gn = {}, // group nodes d'avant\n" +
"                gc = {}, // group centroids d'avant\n" +
"                nodes = [], // nodes de sortie\n" +
"                links = []; // links de sortie\n" +
"\n" +
"            // Procedure des noeuds d'avant pour réutiliser ou calculation du centroid\n" +
"            if (prev) {\n" +
"                prev.nodes.forEach(function(n) {\n" +
"                    var i = index(n),\n" +
"                        o;\n" +
"                    if (n.size > 0) {\n" +
"                        gn[i] = n;\n" +
"                        n.size = 0;\n" +
"                    } else {\n" +
"                        o = gc[i] || (gc[i] = {\n" +
"                            x: 0,\n" +
"                            y: 0,\n" +
"                            count: 0\n" +
"                        });\n" +
"                        o.x += n.x;\n" +
"                        o.y += n.y;\n" +
"                        o.count += 1;\n" +
"                    }\n" +
"                });\n" +
"            }\n" +
"\n" +
"            // Détermination des noeuds\n" +
"            for (var k = 0; k < data.nodes.length; ++k) {\n" +
"                var n = data.nodes[k],\n" +
"                    i = index(n),\n" +
"                    l = gm[i] || (gm[i] = gn[i]) || (gm[i] = {\n" +
"                        group: i,\n" +
"                        name: n.name,\n" +
"                        gene: n.gene,\n" +
"                        level: n.level,\n" +
"                        value: n.value,\n" +
"                        size: 0,\n" +
"                        nodes: []\n" +
"                    });\n" +
"\n" +
"                if (expand[i]) {\n" +
"                    // Le noeud doit être directement visible\n" +
"                    nm[n.name] = nodes.length;\n" +
"                    nodes.push(n);\n" +
"                    if (gn[i]) {\n" +
"                        // Placer nouveaux noeuds au cluster\n" +
"                        n.x = gn[i].x + Math.random();\n" +
"                        n.y = gn[i].y + Math.random();\n" +
"                    }\n" +
"                } else {\n" +
"                    // Le noeud fait partie d'un cluster demonté\n" +
"                    if (l.size == 0) {\n" +
"                        // Si le cluster est nouveau, ajouter et positionner au centroid du noeud feuille\n" +
"                        nm[i] = nodes.length;\n" +
"                        nodes.push(l);\n" +
"                        if (gc[i]) {\n" +
"                            l.x = gc[i].x / gc[i].count;\n" +
"                            l.y = gc[i].y / gc[i].count;\n" +
"                        }\n" +
"                    }\n" +
"                    l.nodes.push(n);\n" +
"                }\n" +
"                // Toujours compter la taille du groupe pour l'utiliser après dans les calculs de force du graphe\n" +
"                l.size += 1;\n" +
"                n.group_data = l;\n" +
"            }\n" +
"\n" +
"            for (i in gm) {\n" +
"                gm[i].link_count = 0;\n" +
"            }\n" +
"\n" +
"            // Déterminer links\n" +
"            for (k = 0; k < data.links.length; ++k) {\n" +
"                var e = data.links[k],\n" +
"                    u = index(e.source),\n" +
"                    v = index(e.target);\n" +
"                if (u != v) {\n" +
"                    gm[u].link_count++;\n" +
"                    gm[v].link_count++;\n" +
"                }\n" +
"                u = expand[u] ? nm[e.source.name] : nm[u];\n" +
"                v = expand[v] ? nm[e.target.name] : nm[v];\n" +
"                var i = u + \"|\" + v,\n" +
"                    l = lm[i] || (lm[i] = {\n" +
"                        source: v,\n" +
"                        target: u,\n" +
"                        value: e.value,\n" +
"                        type: e.type,\n" +
"                        size: 0\n" +
"                    });\n" +
"                l.size += 1;\n" +
"            }\n" +
"            for (i in lm) {\n" +
"                links.push(lm[i]);\n" +
"            }\n" +
"\n" +
"            return {\n" +
"                nodes: nodes,\n" +
"                links: links\n" +
"            };\n" +
"        }\n" +
"\n" +
"        function convexHulls(nodes, index, offset) {\n" +
"            var hulls = {};\n" +
"\n" +
"            // Créer ensembles de points\n" +
"            for (var k = 0; k < nodes.length; ++k) {\n" +
"                var n = nodes[k];\n" +
"                if (n.size) continue;\n" +
"                var i = index(n),\n" +
"                    l = hulls[i] || (hulls[i] = []);\n" +
"                l.push([n.x - offset, n.y - offset]);\n" +
"                l.push([n.x - offset, n.y + offset]);\n" +
"                l.push([n.x + offset, n.y - offset]);\n" +
"                l.push([n.x + offset, n.y + offset]);\n" +
"            }\n" +
"\n" +
"            // Créer les engoblements\n" +
"            var hullset = [];\n" +
"            for (i in hulls) {\n" +
"                hullset.push({\n" +
"                    group: i,\n" +
"                    path: d3.geom.hull(hulls[i])\n" +
"                });\n" +
"            }\n" +
"\n" +
"            return hullset;\n" +
"        }\n" +
"\n" +
"		//Déssiner engoblement\n" +
"        function drawCluster(d) {\n" +
"            return curve(d.path); // 0.8 souhaité\n" +
"        }\n" +
"\n" +
"        //Fonction de Zoom\n" +
"        function zoom() {\n" +
"            vis.attr(\"transform\", \"translate(\" + d3.event.translate + \")scale(\" + d3.event.scale + \")\");\n" +
"        }\n" +
"\n" +
"        // --------------------------------------------------------\n" +
"\n" +
"        var compteur = 0;\n" +
"\n" +
"        var body = d3.select(\"body\");\n" +
"\n" +
"		//Création du SVG et ses propriétés\n" +
"        var vis = body.append(\"svg\")\n" +
"            .attr(\"width\", window.innerWidth - 50) //largeur d'écran\n" +
"            .attr(\"height\", window.innerHeight - 220) //hauteur d'écran\n" +
"            .append(\"g\")\n" +
"            .call(d3.behavior.zoom().scaleExtent([-8, 8]).on(\"zoom\", zoom))\n" +
"            .append(\"g\");\n" +
"\n" +
"        // Construction des flèches et ses propriétés\n" +
"        vis.append(\"svg:defs\").selectAll(\"marker\")\n" +
"            .data([\"marker0\", \"marker1\", \"marker2\"]) //types d'extremité de flèche\n" +
"            .enter().append(\"svg:marker\")\n" +
"            .attr(\"id\", function(d) {\n" +
"                return d;\n" +
"            })\n" +
"            .attr(\"viewBox\", \"0 -5 10 10\")\n" +
"            .attr(\"refX\", 19)\n" +
"            .attr(\"refY\", 0)\n" +
"            //Dimensions des flèches\n" +
"            .attr(\"markerWidth\", 6)\n" +
"            .attr(\"markerHeight\", 6)\n" +
"            .attr(\"orient\", \"auto\")\n" +
"            .append(\"svg:path\")\n" +
"            .attr(\"d\", \"M0,-5L10,0L0,5\");\n" +
"\n" +
"        d3.json(\""+nomAN+".json\", function(json) {\n" +                      // mettre le bon nom du nouveau fichier .json
"            data = json;\n" +
"\n" +
"			//Créer la liste du bouton chercher\n" +
"			\n" +
"            function createSelect(options) {\n" +
"                $.each(options, function(index, item) {\n" +
"                    $(\"#search\").append($(\"<option></option>\").attr(\"value\", item.name).text(item.name));\n" +
"\n" +
"                });\n" +
"            }\n" +
"\n" +
"            $(document).ready(function() {\n" +
"                createSelect(json.nodes);\n" +
"            });\n" +
"			\n" +
"			//Union des noeuds et liens\n" +
"			\n" +
"            for (var i = 0; i < data.links.length; ++i) {\n" +
"                o = data.links[i];\n" +
"                o.source = data.nodes[o.source];\n" +
"                o.target = data.nodes[o.target];\n" +
"            }\n" +
"\n" +
"            hullg = vis.append(\"g\");\n" +
"            linkg = vis.append(\"g\");\n" +
"            nodeg = vis.append(\"g\");\n" +
"\n" +
"			//Fonction d'initialisation de la boucle infinie\n" +
"            init();\n" +
"\n" +
"            vis.attr(\"opacity\", 1e-6)\n" +
"                .transition()\n" +
"                .duration(1000)\n" +
"                .attr(\"opacity\", 1);\n" +
"\n" +
"        });\n" +
"\n" +
"		//Fonction principale\n" +
"        function init() {\n" +
"            if (force) force.stop();\n" +
"\n" +
"			//Acquisition du réseau\n" +
"            net = network(data, net, getGroup, expand);\n" +
"\n" +
"			//Établissement des paramètres du force layour\n" +
"            force = d3.layout.force()\n" +
"                .nodes(net.nodes)\n" +
"                .links(net.links)\n" +
"                .size([window.innerWidth-50, window.innerHeight-220])\n" +
"                .linkDistance(function(l, i) {\n" +
"                    var n1 = l.source,\n" +
"                        n2 = l.target;\n" +
"                    // Distance supérieure pour les grans groupes\n" +
"                    // Distance petite si noeud a très peu de liens\n" +
"					//Configuration de la distance pour les différents cas\n" +
"                    return 180 +\n" +
"                        Math.min(40 * Math.min((n1.size || (n1.group != n2.group ? n1.group_data.size : 0)), (n2.size || (n1.group != n2.group ? n2.group_data.size : 0))), -60 +\n" +
"                            60 * Math.min((n1.link_count || (n1.group != n2.group ? n1.group_data.link_count : 0)), (n2.link_count || (n1.group != n2.group ? n2.group_data.link_count : 0))),\n" +
"                            100);\n" +
"                    //return 150;\n" +
"                })\n" +
"                .linkStrength(function(l, i) {\n" +
"                    return 1;\n" +
"                })\n" +
"                //Joueur avec les trois paramètres pour trouver la bonne visualisation\n" +
"                .gravity(0.025) // gravité du layout entre les noeuds\n" +
"                .charge(-4000) // force entre les noeuds\n" +
"                .friction(0.5) // friction fait les noeuds \"sauter\" et empèche la collision\n" +
"                .start();\n" +
"\n" +
"			//Création des engoblements\n" +
"            hullg.selectAll(\"path.hull\").remove();\n" +
"            hull = hullg.selectAll(\"path.hull\")\n" +
"                .data(convexHulls(net.nodes, getGroup, off))\n" +
"                .enter().append(\"path\")\n" +
"                .attr(\"class\", \"hull\")\n" +
"                .attr(\"d\", drawCluster)\n" +
"                //Couleur\n" +
"                .style(\"fill\", function(d) {\n" +
"                    return fill(d.group);\n" +
"                })\n" +
"                //Grouper\n" +
"                .on(\"click\", function(d) {\n" +
"                    console.log(\"hull click\", d, arguments, this, expand[d.group]);\n" +
"                    expand[d.group] = false;\n" +
"                    init();\n" +
"                });\n" +
"\n" +
"			//Création des liens\n" +
"            link = linkg.selectAll(\"path.link\").data(net.links, linkid);\n" +
"            link.exit().remove();\n" +
"            link.enter().append(\"path\")\n" +
"                .attr(\"class\", function(d) {\n" +
"                    return \"link marker\" + d.value;\n" +
"                })\n" +
"                //Courbe et position des flèches\n" +
"                .attr(\"d\", function(d) {\n" +
"                    var dx = d.target.x - d.source.x;\n" +
"                    var dy = d.target.y - d.source.y;\n" +
"                    if (d.type == 1) {\n" +
"                        var dr = Math.sqrt(dx * dx + dy * dy);\n" +
"                    } else {\n" +
"                        var dr = 0;\n" +
"                    }\n" +
"                    return \"M\" + d.source.x + \",\" + d.source.y +\n" +
"                        \"A\" + dr + \",\" + dr + \" 0 0 1,\" + d.target.x + \",\" + d.target.y +\n" +
"                        \"A\" + dr + \",\" + dr + \" 0 0 0,\" + d.source.x + \",\" + d.source.y;\n" +
"                })\n" +
"                .attr(\"marker-end\", function(d) {\n" +
"                    if (d.value == 3) {\n" +
"                        return null;\n" +
"                    } else {\n" +
"                        return \"url(#marker\" + d.value + \")\";\n" +
"                    }\n" +
"                })\n" +
"                .attr(\"stroke-width\", 2)\n" +
"                //Events de passage de mouse\n" +
"                .on(\"mouseover\", function(d) {\n" +
"                	//Shift appuyé\n" +
"                    if (d3.event.shiftKey) {\n" +
"                        d3.select(this).classed('action', false);\n" +
"                        d3.select(this).attr(\"marker-end\", \"url(#marker2)\");\n" +
"                        if (d.source.group === d.target.group) {\n" +
"                            d3.select(this).classed('transition_action', false);\n" +
"                        }\n" +
"                        d3.select(this).classed('recent', true);\n" +
"                        if (d.source.group === d.target.group) {\n" +
"                            d3.select(this).classed('transition_recent', true);\n" +
"                        }\n" +
"                        if (d.value == 3) {\n" +
"                            d3.select(this).attr(\"marker-end\", null);\n" +
"                        }\n" +
"                    }\n" +
"                    //CTRL appuyé\n" +
"                    if (d3.event.ctrlKey) {\n" +
"                        d3.select(this).attr(\"marker-end\", \"url(#marker0)\");\n" +
"                        d3.select(this).classed('recent', false);\n" +
"                        if (d.source.group === d.target.group) {\n" +
"                            d3.select(this).classed('transition_recent', false);\n" +
"                        }\n" +
"                        d3.select(this).classed('action', false);\n" +
"                        if (d.source.group === d.target.group) {\n" +
"                            d3.select(this).classed('transition_action', false);\n" +
"                        }\n" +
"                        if (d.value == 3) {\n" +
"                            d3.select(this).attr(\"marker-end\", null);\n" +
"                        }\n" +
"                    }\n" +
"                    //ALT appuyé\n" +
"                    if (d3.event.altKey) {\n" +
"                        d3.select(this).classed('recent', false);\n" +
"                        d3.select(this).attr(\"marker-end\", \"url(#marker1)\");\n" +
"                        if (d.source.group === d.target.group) {\n" +
"                            d3.select(this).classed('transition_recent', false);\n" +
"                        }\n" +
"                        d3.select(this).classed('action', true);\n" +
"                        if (d.source.group === d.target.group) {\n" +
"                            d3.select(this).classed('transition_action', true);\n" +
"                        }\n" +
"                        if (d.value == 3) {\n" +
"                            d3.select(this).attr(\"marker-end\", null);\n" +
"                        }\n" +
"                    }\n" +
"                })\n" +
"                //Types de flèches\n" +
"                .attr('stroke', function(d) {\n" +
"                    if (d.source.group === d.target.group) {\n" +
"                        d3.select(this).classed('transition', true);\n" +
"                    }\n" +
"                    if (d.value == 1) {\n" +
"                        d3.select(this).classed('action', true);\n" +
"                        if (d.source.group === d.target.group) {\n" +
"                            d3.select(this).classed('transition_action', true);\n" +
"                        }\n" +
"                    }\n" +
"                    if (d.value == 2) {\n" +
"                        d3.select(this).classed('recent', true);\n" +
"                        if (d.source.group === d.target.group) {\n" +
"                            d3.select(this).classed('transition_recent', true);\n" +
"                        }\n" +
"                    }\n" +
"                });\n" +
"			\n" +
"			//Création des noeuds\n" +
"            node = nodeg.selectAll(\"g.node\").data(net.nodes, nodeid);\n" +
"            node.exit().remove();\n" +
"            var onEnter = node.enter();\n" +
"            var g = onEnter\n" +
"                .append(\"g\")\n" +
"                .attr(\"class\", function(d) {\n" +
"                    return \"node\" + (d.size ? \"\" : \" leaf\");\n" +
"                })\n" +
"                .attr(\"transform\", function(d) {\n" +
"                    return \"translate(\" + d.x + \",\" + d.y + \")\";\n" +
"                });\n" +
"\n" +
"			//Création des flèches\n" +
"            g.append(\"circle\")\n" +
"                // if (d.size) -- d.size > 0 when d is a group node.      \n" +
"                .attr(\"r\", function(d) {\n" +
"                    return d.size ? d.size + dr : dr + 1;\n" +
"                })\n" +
"                .style(\"fill\", function(d) {\n" +
"                    return fill(d.group);\n" +
"                })\n" +
"                .style(\"stroke\", \"black\")\n" +
"                //Gène actif ou pas\n" +
"                .attr('stroke-width', function(d) {\n" +
"                    if (d.value == 1) {\n" +
"                        return 2;\n" +
"                    } else {\n" +
"                        return 0;\n" +
"                    }\n" +
"                })\n" +
"                //Fixer le noeud après lâcher la souris\n" +
"                .on(\"mousedown\", function(d) {\n" +
"                    d.fixed = true;\n" +
"                })\n" +
"                //Event de passage de la souris - active ou pas\n" +
"                .on(\"mouseover\", function(d) {\n" +
"                    if (d3.event.shiftKey) {\n" +
"                        d3.select(this).classed('non-active', false);\n" +
"                        d3.select(this).classed('active', true);\n" +
"                    }\n" +
"                    if (d3.event.ctrlKey) {\n" +
"                        d3.select(this).classed('active', false);\n" +
"                        d3.select(this).classed('non-active', true);\n" +
"                    }\n" +
"                })\n" +
"                //Clicker pour démonter le gène\n" +
"                .on(\"click\", function(d) {\n" +
"                    expand[d.group] = !expand[d.group];\n" +
"                    init();\n" +
"                });\n" +
"\n" +
"			//Création du texte à côté du noeud pour les différents cas\n" +
"            g.append(\"text\")\n" +
"                .text(function(d, i) {\n" +
"                    if (!d.size) {\n" +
"                        if (d.level == 0) {\n" +
"                            return d.level + \" . . . \" + d.gene;\n" +
"                        } else {\n" +
"                            return d.level;\n" +
"                        }\n" +
"                    } else {\n" +
"                        return \". . . \" + d.gene;\n" +
"                    }\n" +
"                });\n" +
"\n" +
"            node.call(force.drag);\n" +
"\n" +
"			//Fonction tick qui est lancé à tout instant et contrôle les positions du force layout\n" +
"            force.on(\"tick\", function() {\n" +
"            	//Gestion des engoblements\n" +
"                if (!hull.empty()) {\n" +
"                    hull.data(convexHulls(net.nodes, getGroup, off))\n" +
"                        .attr(\"d\", drawCluster);\n" +
"                }\n" +
"				\n" +
"				//Gestion des liens\n" +
"                link.attr(\"d\", function(d) {\n" +
"                    var dx = d.target.x - d.source.x;\n" +
"                    var dy = d.target.y - d.source.y;\n" +
"                    if (d.type == 1) {\n" +
"                        var dr = Math.sqrt(dx * dx + dy * dy);\n" +
"                    } else {\n" +
"                        var dr = 0;\n" +
"                    }\n" +
"\n" +
"                    return \"M\" + d.source.x + \",\" + d.source.y +\n" +
"                        \"A\" + dr + \",\" + dr + \" 0 0 1,\" + d.target.x + \",\" + d.target.y +\n" +
"                        \"A\" + dr + \",\" + dr + \" 0 0 0,\" + d.source.x + \",\" + d.source.y;\n" +
"                });\n" +
"				\n" +
"				//Gestion des noeuds\n" +
"                node.attr(\"transform\", function(d) {\n" +
"                    return \"translate(\" + d.x + \",\" + d.y + \")\";\n" +
"                });\n" +
"				\n" +
"				//compteur du nombre de fois passées\n" +
"                compteur = compteur + 1;\n" +
"                if (compteur == 1) {\n" +
"                    $(\"circle\").d3Click();\n" +
"                }\n" +
"\n" +
"            });\n" +
"        }\n" +
"		\n" +
"		//Fonction de recherche d'un gène\n" +
"        function searchNode() {\n" +
"            //trouver le noeud\n" +
"            var selectedVal = document.getElementById('search').value;\n" +
"            var node = vis.selectAll(\"circle\");\n" +
"            if (selectedVal == \"none\") {\n" +
"                node.style(\"stroke\", \"white\").style(\"stroke-width\", \"1\");\n" +
"            } else {\n" +
"                var selected = node.filter(function(d, i) {\n" +
"                    return d.name != selectedVal;\n" +
"                });\n" +
"                //Changer toutes les opacités des autres éléments\n" +
"                selected.style(\"opacity\", \"0\");\n" +
"                var link = vis.selectAll(\"path.link\");\n" +
"                link.style(\"opacity\", \"0\");\n" +
"                var text = vis.selectAll(\"text\")\n" +
"                text.style(\"opacity\", \"0\");\n" +
"                var hull = vis.selectAll(\"path.hull\")\n" +
"                hull.style(\"opacity\", \"0\");\n" +
"                //Transition lente - Tout comme avant\n" +
"                d3.selectAll(\"circle, path.link, text, path.hull\").transition()\n" +
"                    .duration(5000)\n" +
"                    .style(\"opacity\", 1);\n" +
"            }\n" +
"        }\n" +
"    </script>\n" +
"</body>\n" +
"\n" +
"</html>"; //fin du texte html...
            
            writerHTML.write(texteHTML);   // écriture

            writerHTML.close();  // pas oublié de fermer le flux (serait mieux dans le finally
            logger.info("Ficher " + nomAN + ".html écrit.");
            
        } catch (IOException e2) {
            System.err.println("Problème d'écriture du nouveau fichier .html.");
            e2.printStackTrace();
        } finally {
//useless for the moment
        }
    }
     
    /**
     * donne juste le numéro du noeud de l'état du gène
     *
     * @param l
     * @param nom
     * @return
     */
    public static int nomNumero(ArrayList<String> l, String nom) {
        int i = 0;
        while (!l.get(i).equals(nom) && i < l.size()) {
            i++;
        }
        if (i == l.size()) {
            logger.severe("ERREUR: dans nomNumero, Dépassement de la taille de la liste!!!");
        }
        return i;
        /* for (Iterator<String> it = nomCoop.iterator(); it.hasNext();) {
                        nomscoop += it.next();
                    }*/
    }
}

//{
//  "nodes":[
//    {"name":"BMAL1_cyt.0","group":1},
//    {"name":"BMAL1_cyt.1","group":1},

