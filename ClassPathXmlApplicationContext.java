import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ClassPathXmlApplicationContext implements BeanFactory {


    private Map<String, Object> beanMap = new HashMap<>();

    public ClassPathXmlApplicationContext() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        //调用大的class获取applicationContext.xml流文件
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("applicationContext.xml");
        //创建DocumentBuilderFactory类为后面的解析做铺垫；
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        //创建DocumentBuilder对象为后面的解析做铺垫；
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        //获取Document对象来解析当前流；
        Document document = null;
        try {
            document = documentBuilder.parse("applicationContext.xml");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
        }
        //获取applicationContext.xml所有的bean节点；
        NodeList bean = document.getElementsByTagName("bean");
        for (int i = 0; i < bean.getLength(); i++) {
            //获取bean里面的所有元素
            Node item = bean.item(i);
            //判断节点是否是元素节点
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                //将item强转为元素节点(Element)为元素节点
                Element item1 = (Element) item;
                //获取applicationContext.xml中id
                String beanid = item1.getAttribute("id");
                //获取applicationContext.xml中class
                String clazz = item1.getAttribute("class");

                //对应calss的实例对象
                Object beanobj = null;
                try {
                    //创建bean的实例
                    beanobj = Class.forName(clazz).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                //放入到bean的实例对象放入map容器中；
                Object put = beanMap.put(beanid, beanobj);
            }
        }
        //5.组织bean之间的依赖关系
        for (int i = 0; i < bean.getLength(); i++) {
            Node item = bean.item(i);
            //判断节点是否是元素节点
            if (item.getNodeType() == Node.ELEMENT_NODE) {

                //将item强转为元素节点(Element)为元素节点
                Element beanitem1 = (Element) item;
                String beanid = beanitem1.getAttribute("id");
                //获取他的子节点
                NodeList beanchildNodes = beanitem1.getChildNodes();
                //获取applicationContext.xml中id
                for (int j = 0; j < beanchildNodes.getLength(); j++) {
                    Node beanitem2 = beanchildNodes.item(j);
                    if (beanitem2.getNodeType()==Node.ELEMENT_NODE && "property".equals(beanitem2.getNodeName())){
                        Element beanitem21 = (Element) beanitem2;
                        //取出name，和ref的属性
                        String peanname = beanitem21.getAttribute("name");
                        String peanref =beanitem21.getAttribute("ref");
                        //1）通过ref找到实例，
                        Object refobj = beanMap.get(peanref);
                        //2）将ref设置到当前bean对应的实例的property属性上通过反射
                        Object beanidObj = beanMap.get(beanid);
                        //通过反射
                        Class<?> peanClass = beanidObj.getClass();
                        //找到property的实例
                        Field prorpertyField = peanClass.getDeclaredField(peanname);
                        //强制访问
                        prorpertyField.setAccessible(true);
                        //放入
                        prorpertyField.set(beanid,refobj);

                    }
                }



            }

        }

    }

    @Override
    public Object getBean(String id) {
        return beanMap.get(id);
    }
}