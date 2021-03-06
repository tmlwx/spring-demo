package com.bailiban.day1.mydi.setterdemo;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanFactory {

    private static Map<String, Object> beanMap = new HashMap<>();

    public static void refresh(String xmlPath) throws Exception {
        // 创建SAXReader
        SAXReader saxReader = new SAXReader();
        // 获取xml配置文件
        URL xmlUrl = BeanFactory.class.getClassLoader().getResource(xmlPath);
        // 将SAXReader和xml配置文件绑定
        Document doc = saxReader.read(xmlUrl);
        // 获取根元素“beans”
        Element root = doc.getRootElement();
        // 遍历bean节点
        List<Element> beanElementList = root.elements();
        for (Element beanElement: beanElementList) {
            parseBeanElement(beanElement);
        }
    }

    private static void parseBeanElement(Element beanElement) throws Exception {
        String id = beanElement.attributeValue("id");
        String clsName = beanElement.attributeValue("class");
        // 获取Class对象
        Class<?> cls = Class.forName(clsName);
        // 直接调用无参构造函数，实例化一个对象
        Object beanObj = cls.getDeclaredConstructor().newInstance();
        beanMap.put(id, beanObj);

        // 获取属性节点，并调用setter方法设置属性
        List<Element> subElemList = beanElement.elements();
        for (Element subElem: subElemList) {
            // 获取属性名称
            String name = subElem.attributeValue("name");
            // 获取属性值
            String ref = subElem.attributeValue("ref");
            Object refObj = beanMap.get(ref);
            // 根据属性名称构造setter方法名： set + 属性首字母大写 + 属性其他字符，例：setUserDao
            String methodName = "set" + (char)(name.charAt(0) - 32) + name.substring(1);
            // 获取Method对象
            Method method = cls.getDeclaredMethod(methodName, refObj.getClass().getInterfaces()[0]);
            // 调用setter方法，设置对象属性
            method.invoke(beanObj, refObj);
        }
    }

    public static Object getBean(String beanId) {
        return beanMap.get(beanId);
    }
}
