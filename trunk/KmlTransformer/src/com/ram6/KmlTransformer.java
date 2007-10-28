package com.ram6;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KmlTransformer {
  
  private File file;

  public KmlTransformer(File file) {
    this.file = file;
  }
  
	public void transform() throws Exception {
	  Document doc = readDoc();

	  // clone boundaries
	  List<Node> boundaries = getNodes(doc.getElementsByTagName("outerBoundaryIs"), true);
	  
	  // remove all folders, but first clone relevant first folder children
	  List<Node> folders = getNodes(doc.getElementsByTagName("Folder"), false);
	  List<Node> firstFolderChildren = cloneFolderChildren(folders);
	  for (Node folder : folders) {
	    folder.getParentNode().removeChild(folder);
    }
	  
	  // add boundaries in a new folder
	  Node root = doc.getElementsByTagName("Document").item(0);
	  Element folder = doc.createElement("Folder");
	  root.appendChild(folder);
	  for (Node folderChild : firstFolderChildren) {
	    folder.appendChild(folderChild);
	  }
	  Element placemark = doc.createElement("Placemark");
	  folder.appendChild(placemark);
	  Element polygon = doc.createElement("Polygon");
	  for (Node node : boundaries) {
	    polygon.appendChild(node);
	  }
	  placemark.appendChild(polygon);
	  
	  writeDoc(doc);
	}

	private List<Node> cloneFolderChildren(List<Node> folders) {
	  List<Node> nodes = new ArrayList<Node>();
	  Node folderName = null;
	  Node visibility = null;
	  Node open = null;
    Node folder = folders.get(0);
    List<Node> children = getNodes(folder .getChildNodes(), false);
    for (Node child : children) {
      if (folderName == null && child.getNodeName().equals("name")) {
        folderName = child.cloneNode(true);
      }
      if (visibility == null && child.getNodeName().equals("visibility")) {
        visibility = child.cloneNode(true);
      }
      if (open == null && child.getNodeName().equals("open")) {
        open = child.cloneNode(true);
      }
    }
    if (folderName != null){
      nodes.add(folderName);
    }
    if (visibility != null){
      nodes.add(visibility);
    }
    if (open != null){
      nodes.add(open);
    }
	  return nodes;
	}

  private List<Node> getNodes(NodeList nodeList, boolean clone) {
    List<Node> nodes = new ArrayList<Node>();
	  for (int i = 0; i < nodeList.getLength(); i++) {
	    Node node = nodeList.item(i);
	    if (clone) {
	      node = node.cloneNode(true);
	    }
      nodes.add(node);
	  }
	  return nodes;
  }
	
	private Document readDoc() throws Exception {
		DocumentBuilderFactory factory =
			DocumentBuilderFactory.newInstance();
			// factory.setValidating(true); 
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(file);
	}
	
	private void writeDoc(Document doc) throws Exception {
	  doc.normalize();
	  Source domSource = new DOMSource(doc);
	  File outFile = new File(file.getAbsoluteFile() + ".out");
    Result fileResult = new StreamResult(outFile);
	  TransformerFactory factory = TransformerFactory.newInstance();
	  Transformer transformer = factory.newTransformer();
	  transformer.transform(domSource, fileResult);
	  System.out.println("Successfully wrote file: " + outFile.getAbsolutePath());
	}
	
	
	public static void main(String[] args) throws Exception {
	  if (args.length != 1) {
	    System.out.println("Please specify the path to the file to transform.");
	    System.exit(1);
	  }
	  File file = new File(args[0]);
	  if (!file.exists()) {
	    System.out.println("File " + file.getAbsolutePath() + " does not exist.");
	    System.exit(1);
	  }
	  KmlTransformer transformer = new KmlTransformer(file);
	  transformer.transform();
  }
}
