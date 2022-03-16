import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@WebServlet("*.do")
public class DispatcaherServlet中央控制器04 extends  ViewBaseServlet{
    private BeanFactory beanFactory;
    @Override
    /*
    初始化配置
     */
    public void init(){
        super.init();
        try {
            beanFactory=new ClassPathXmlApplicationContext();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /*
    * 服务配置
    * */
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //设置编码
        req.setCharacterEncoding("UTF-8");
        //假设servletPath是/hello.do
        String servletPath = req.getServletPath();//注：servletPath就是相对于是id，从而获取value；
        //获取索引
        int i = servletPath.lastIndexOf(".do");
        //把/hello.do变成hello；
        String substring = servletPath.substring(1, i);
        //相当于value；
        Object controllerBeanObj = beanFactory.getBean(substring);
        //获取operate的数据
        String operate = req.getParameter("operate");//注：调用的方法根据operate这个值确定，operate相当于name；
        if (operate == null) {
            operate="index";
        }
        //获取当前类的所有方法

        try {
            Method[] methods = controllerBeanObj.getClass().getDeclaredMethods();
            for (Method method:methods
                 ) {
                if (operate.equals(method.getName())) {
                    //1.统一获取请求参数
                    //获取当前方法的参数，返回数组类型
                    Parameter[] parameters = method.getParameters();//获取方法的实际参数（jdk8以后）注：获取后需重写导入out下的文件；
                    //paramaterValues用来存放参数值
                    Object[] paramaterValues = new Object[parameters.length];
                    //相当于把多个req.getParameter完参数放入到paramaterValues；
                    String parameter1 = null;
                    for (int j = 0; j < parameters.length; j++) {
                        Parameter parameter = parameters[i];
                        String parameterName = parameter.getName();
                        if ("req".equals(parameterName)) {
                            paramaterValues[i] = req;
                        } else if ("resp".equals(parameterName)) {
                            paramaterValues[i] = resp;
                        } else if ("session".equals(parameterName)) {
                            paramaterValues[i] = req.getSession();
                        } else {
                            //从请求中获取参数数值
                         String   paramaterValue = req.getParameter(parameter.getName());
                            String typeName = parameter.getType().getName();
                            // 解决bag报参数类型不匹配
                            Object paramaterObj=paramaterValue;
                            if(paramaterObj!=null ){
                                if ("java.lang.Integer".equals(typeName)){
                                    paramaterObj = Integer.parseInt(paramaterValue);
                                }
                            }


                            paramaterValues[i] = paramaterObj;//"2"，而不是  2  报参数类型不匹配






                        }

                    }


                    // 2.controller组件中方法的调用
                    method.setAccessible(true);
                    String methodReturnStr = (String) method.invoke(controllerBeanObj, paramaterValues);//paramaterValues放的是实参

                    // 3.试图处理
                    if (methodReturnStr == "redirect:")//判断是否redirect:开头
                    {
                        String substring1 = methodReturnStr.substring("redirect:".length());
                        resp.sendRedirect(substring1);
                    } else {
                        super.processTemplate("methodReturnStr", req, resp);
                    }

                }else {
                    throw new RuntimeException("operate为非法值");
                }
                }

            } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }
        //方式二
//        Method[] method = controllerBeanObj.getClass().getDeclaredMethods();
//        for (Method m :method
//             ) {
//            //获取方法名称
//            String name = m.getName();
//            if (operate == name) {
//                try {
//                    m.invoke(this,req,resp);
//                    return;
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//
//            }
//            throw new RuntimeException("operate为非法值");
//
//        }




    }

