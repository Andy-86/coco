package com.devicemanagement.util;


import android.os.Environment;

import com.devicemanagement.BuildConfig;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Administrator on 2017/7/1 0001.
 */
public class ParseXml {

    /* 保存解析的XML信息 */
    private static HashMap<String, String> mHashMap;

    private static boolean isReadSucceed;

    private static String filepath;
    /**
     * 采取DOM的形式解析XML
     * @param localpath
     * @return
     * @throws Exception
     */
    public static HashMap<String, String> parseXml(String localpath) throws Exception
    {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        String path = localpath + "/update.xml";
        File update_xml = new File(path);

        //获取文件输入流
        InputStream inStream = new FileInputStream(update_xml);
        if(inStream==null) return null;
        // 实例化一个文档构建器工厂
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 通过文档构建器工厂获取一个文档构建器
        DocumentBuilder builder = factory.newDocumentBuilder();
        // 通过文档通过文档构建器构建一个文档实例

        Document document = builder.parse(inStream);
        //获取XML文件根节点
        Element root = document.getDocumentElement();
        //获得所有子节点
        NodeList childNodes = root.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++)
        {
            //遍历子节点
            Node childNode = (Node) childNodes.item(j);
            if (childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childElement = (Element) childNode;
                //版本号
                if ("version".equals(childElement.getNodeName()))
                {
                    hashMap.put("version",childElement.getFirstChild().getNodeValue());
                }
                //软件名称
                else if (("name".equals(childElement.getNodeName())))
                {
                    hashMap.put("name",childElement.getFirstChild().getNodeValue());
                }
                //下载地址
                else if (("url".equals(childElement.getNodeName())))
                {
                    hashMap.put("url",childElement.getFirstChild().getNodeValue());
                }
            }
        }
        return hashMap;
    }

    /**
     * 获取软件版本号
     * @return
     */
    public static int getVersionCode()
    {
        int versionCode = 0;
        //Android Studio获取方式
        versionCode = BuildConfig.VERSION_CODE;
        //String versionName = BuildConfig.VERSION_NAME;
        return versionCode;
    }
    /**
     * 检查软件是否有更新版本
     *
     * @return
     */
    public static boolean isUpdate(String deviceDirectory)
    {
        // 获取当前软件版本
        int versionCode = getVersionCode();

        try { //一个测试
            mHashMap = parseXml(Environment.getExternalStorageDirectory().getPath() + deviceDirectory);//得到解析信息
        }
        catch (Exception e) {
            e.printStackTrace();//测试失败
        }
        if (null != mHashMap)
        {
            int serviceCode = Integer.valueOf(mHashMap.get("version"));
            // 版本判断
            if (serviceCode > versionCode)
            {
                return true;
            }
        }
        return false;
    }

}
