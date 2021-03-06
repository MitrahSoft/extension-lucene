package org.lucee.extension.search.lucene;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;

import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.net.http.HTTPResponse;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.util.IO;

import org.lucee.extension.search.lucene.docs.FieldUtil;
import org.lucee.extension.search.lucene.docs.FileDocument;
import org.lucee.extension.search.lucene.docs.HTMLDocument;
import org.lucee.extension.search.lucene.docs.PDFDocument;
import org.lucee.extension.search.lucene.docs.WordDocument;

/**
 * creates a matching Document Object to given File
 */
public final class DocumentUtil {

	public static Document toDocument(StringBuffer content,String root,URL url, HTTPResponse method) throws IOException, PageException {
        CFMLEngine e = CFMLEngineFactory.getInstance();
		IO io = e.getIOUtil();
		
		if(method.getStatusCode()!=200)return null;
        
		// get type and charset
		Document doc=null;
		ContentType ct = method.getContentType();
		long len=method.getContentLength();
		String charset=ct==null?"iso-8859-1":ct.getCharset();
        
        Runtime rt = Runtime.getRuntime();
        if(len>rt.freeMemory()){
        	Runtime.getRuntime().gc();
        	if(len>rt.freeMemory()) return null;
        }
        	
        //print.err("url:"+url+";chr:"+charset+";type:"+type);
        
        if(ct==null || ct.getMimeType()==null)  {}
        // HTML
        else if(ct.getMimeType().indexOf("text/html")!=-1) {
        	Reader r=null;
        	try{
        		r = io.getReader(method.getContentAsStream(), e.getCastUtil().toCharset(charset));
        		doc= HTMLDocument.getDocument(content,r);
        	}
        	finally{
        		io.closeSilent(r);
        	}
        }
        // PDF
        else if(ct.getMimeType().indexOf("application/pdf")!=-1) {
        	InputStream is=null;
        	try{
        		is=io.toBufferedInputStream(method.getContentAsStream());
        		doc= PDFDocument.getDocument(content,is);
        	}
        	finally {
        		io.closeSilent(is);
        	}
        }
        // DOC
        else if(ct.getMimeType().equals("application/msword")) {
        	InputStream is=null;
        	try{
        		is=io.toBufferedInputStream(method.getContentAsStream());
        		doc= WordDocument.getDocument(content,is);
        	}
        	finally {
        		io.closeSilent(is);
        	}
            
        }
        // Plain
        else if(ct.getMimeType().indexOf("text/plain")!=-1) {
        	Reader r=null;
        	try{
        		r=io.toBufferedReader(io.getReader(method.getContentAsStream(),e.getCastUtil().toCharset(charset)));
        		doc= FileDocument.getDocument(content,r);
        	}
        	finally {
        		io.closeSilent(r);
        	}
        }
        
        if(doc!=null){
        	String strPath=url.toExternalForm();
    	   
    	    doc.add(FieldUtil.UnIndexed("url", strPath));
    	    doc.add(FieldUtil.UnIndexed("key", strPath));
    	    doc.add(FieldUtil.UnIndexed("path", strPath));
    	    //doc.add(FieldUtil.UnIndexed("size", Caster.toString(file.length())));
    	    //doc.add(FieldUtil.Keyword("modified",DateField.timeToString(file.lastModified())));
        }
        
        return doc;
        
    }
	
    /**
     * translate the file to a Document Object
     * @param file
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws PageException 
     */
    public static Document toDocument(Resource file,String url,String charset) throws IOException, PageException {
    	CFMLEngine e = CFMLEngineFactory.getInstance();
    	
        String ext = e.getResourceUtil().getExtension(file,null);
        
       
        Document doc=null;
        if(ext!=null) {
            ext=ext.toLowerCase();
            //String mimeType=new MimetypesFileTypeMap().getContentType(f);
            // HTML
            if(ext.equals("cfm") || ext.equals("htm") || ext.equals("html") || ext.equals("cfm") || ext.equals("cfml") || ext.equals("php") || ext.equals("asp") || ext.equals("aspx")) {
                doc= HTMLDocument.getDocument(file,charset);
            }
            // PDF
            else if(ext.equals("pdf")) {
                doc= PDFDocument.getDocument(file);
            }
            // DOC
            else if(ext.equals("doc")) {
                doc= WordDocument.getDocument(file);
            }
        }
        else { 
        	ContentType ct = e.getResourceUtil().getContentType(file);
        	String type = ct.getMimeType();
        	String c=ct.getCharset();
        	if(c!=null) charset=c;
            //String type=ResourceUtil.getMimeType(file,"");
            if(type==null)  {}
            // HTML
            else if(type.equals("text/html")) {
                doc= HTMLDocument.getDocument(file,charset);
            }
            // PDF
            else if(type.equals("application/pdf")) {
                doc= PDFDocument.getDocument(file);
            }
            // DOC
            else if(type.equals("application/msword")) {
                doc= WordDocument.getDocument(file);
            }
        }
        if(doc==null) doc= FileDocument.getDocument(file,charset);
        
        String strPath=file.getPath().replace('\\', '/');
	    String strName=strPath.substring(strPath.lastIndexOf('/'));
	    
	    
	    doc.add(FieldUtil.UnIndexed("url", strName));
	    
	    doc.add(FieldUtil.UnIndexed("key", strPath));
	    doc.add(FieldUtil.UnIndexed("path", file.getPath()));
	    doc.add(FieldUtil.UnIndexed("size", e.getCastUtil().toString(file.length())));
	    doc.add(FieldUtil.UnIndexed("modified",DateField.timeToString(file.lastModified())));
        
        
        return doc;
    }
    
}