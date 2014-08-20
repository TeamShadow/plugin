package shadow.plugin.compiler;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

public class ShadowCompilerInterface
{
  private Class<?> parserClass;
  private Class<?> nodeClass;
  private int errorLine;
  private int errorColumn;
  
  public ShadowCompilerInterface()
  {
    try
    {
      ShadowClassLoader loader = new ShadowClassLoader(getClass().getClassLoader());
      this.parserClass = Class.forName("shadow.parser.javacc.ShadowParser", false, loader);
      this.nodeClass = Class.forName("shadow.parser.javacc.Node", false, loader);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      this.parserClass = (this.nodeClass = null);
    }
  }
  
  public int getErrorLine()
  {
    return this.errorLine;
  }
  
  public int getErrorColumn()
  {
    return this.errorColumn;
  }
  
  public Object compile(InputStream input)
  {
    this.errorLine = (this.errorColumn = 0);
    if (this.parserClass != null) {
      try
      {
        Object parser = this.parserClass.getConstructor(new Class[] { InputStream.class })
          .newInstance(new Object[] {input });
        return this.parserClass.getMethod("CompilationUnit", new Class[0])
          .invoke(parser, new Object[0]);
      }
      catch (InvocationTargetException ex)
      {
        Scanner scanner = new Scanner(ex.getTargetException().getMessage());
        scanner.useDelimiter("(Encountered \".*\" at line |, column |\\.)");
        this.errorLine = scanner.nextInt();
        this.errorColumn = scanner.nextInt();
        scanner.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return null;
  }
  
  public Object compile(InputStream input, String encoding)
  {
    this.errorLine = (this.errorColumn = 0);
    if (this.parserClass != null) {
      try
      {
        Object parser = this.parserClass
          .getConstructor(new Class[] {InputStream.class, String.class })
          .newInstance(new Object[] {input, encoding });
        return this.parserClass.getMethod("CompilationUnit", new Class[0])
          .invoke(parser, new Object[0]);
      }
      catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return null;
  }
  
  public Object getParent(Object element)
  {
    if ((this.nodeClass != null) && (this.nodeClass.isInstance(element))) {
      try
      {
        return this.nodeClass.getMethod("jjtGetParent", new Class[0]).invoke(element, new Object[0]);
      }
      catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return null;
  }
  
  public boolean hasChildren(Object element)
  {
    if ((this.nodeClass != null) && (this.nodeClass.isInstance(element))) {
      try
      {
        return ((Integer)this.nodeClass.getMethod("jjtGetNumChildren", new Class[0]).invoke(element, new Object[0])).intValue() != 0;
      }
      catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return false;
  }
  
  public Object[] getChildren(Object element)
  {
    if ((this.nodeClass != null) && (this.nodeClass.isInstance(element))) {
      try
      {
        int numChildren = ((Integer)this.nodeClass.getMethod("jjtGetNumChildren", new Class[0]).invoke(element, new Object[0])).intValue();
        Object[] children = new Object[numChildren];
        for (int i = 0; i < numChildren; i++) {
          children[i] = this.nodeClass.getMethod("jjtGetChild", new Class[] { Integer.TYPE }).invoke(element, new Object[] { Integer.valueOf(i) });
        }
        return children;
      }
      catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return new Object[0];
  }
  
  public int getLine(Object element)
  {
    if ((this.nodeClass != null) && (this.nodeClass.isInstance(element))) {
      try
      {
        return ((Integer)this.nodeClass.getMethod("getLine", new Class[0]).invoke(element, new Object[0])).intValue();
      }
      catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return 0;
  }
  
  public int getColumn(Object element)
  {
    if ((this.nodeClass != null) && (this.nodeClass.isInstance(element))) {
      try
      {
        return ((Integer)this.nodeClass.getMethod("getColumn", new Class[0]).invoke(element, new Object[0])).intValue();
      }
      catch (InvocationTargetException localInvocationTargetException) {}catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return 0;
  }
}
