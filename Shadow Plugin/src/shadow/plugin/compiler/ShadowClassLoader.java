package shadow.plugin.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class ShadowClassLoader
  extends ClassLoader
{
  private IProject project = null;
  
  //This code should be updated to use shadow.jar, wherever it is
  
  public ShadowClassLoader(ClassLoader parent)
  {
    super(parent);
    try
    {
      this.project = ResourcesPlugin.getWorkspace().getRoot()
        .getProject("Shadow");
    }
    catch (NullPointerException localNullPointerException) {}
  }
  
  protected Class<?> findClass(String name)
    throws ClassNotFoundException
  {
    if (this.project != null) {
      try
      {
        String path = name.replace('.', '/').concat(".class");
        IFile file = this.project.getFile("bin/".concat(path));
        if (file.exists()) {
          return loadClass(name, file.getContents());
        }
        Class<?> clazz = checkLib(name, path, 
          this.project.getFolder("lib"));
        if (clazz != null) {
          return clazz;
        }
      }
      catch (ClassNotFoundException ex)
      {
        throw ex;
      }
      catch (Exception ex)
      {
        throw new ClassNotFoundException(name, ex);
      }
    }
    throw new ClassNotFoundException(name);
  }
  
  private Class<?> checkLib(String name, String path, IFolder folder)
    throws ClassNotFoundException, CoreException, IOException
  {
    try
    {
      for (IResource resource : folder.members()) {
        if ((resource instanceof IFile))
        {
          IFile file = (IFile)resource;
          if (file.getName().endsWith(".jar"))
          {
            if (!file.exists()) {
              return null;
            }
            JarInputStream stream = null;
            try
            {
              stream = new JarInputStream(file.getContents());
              JarEntry entry;
              while ((entry = stream.getNextJarEntry()) != null)
              {
                if (path.equals(entry.getName())) {
                  return loadClass(name, stream);
                }
              }
            }
            finally
            {
              try
              {
                if (stream != null) {
                  stream.close();
                }
              }
              catch (IOException localIOException2) {}
            }
            try
            {
              if (stream != null) {
                stream.close();
              }
            }
            catch (IOException localIOException3) {}
          }
        }
      }
    }
    catch (CoreException ex)
    {
      throw new ClassNotFoundException(name, ex);
    }
    return null;
  }
  
  public Class<?> loadClass(String name, InputStream stream)
    throws IOException
  {
    try
    {
      int length = 0;int available = stream.available();
      if (available <= 1) {
        available = 1024;
      }
      byte[] buffer = new byte[available + 1];
      int read = stream.read(buffer);
      while (read != -1)
      {
        length += read;
        available = stream.available();
        if (available == 1) {
          available = length;
        }
        if (buffer.length < length + available)
        {
          byte[] newBuffer = new byte[length + available + 1];
          System.arraycopy(buffer, 0, newBuffer, 0, length);
          buffer = newBuffer;
        }
        read = stream.read(buffer, length, buffer.length - length);
      }
      return defineClass(name, buffer, 0, length);
    }
    finally
    {
      try
      {
        stream.close();
      }
      catch (IOException localIOException2) {}
    }
  }
}
