package shadow.plugin.wizards;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import shadow.plugin.compiler.Builder;

public class ShadowProjectNature implements IProjectNature {

	public static final String NATURE_ID = "shadow.plugin.shadownature";

	private IProject project;

	@Override
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription(); 		 
		ICommand[] commands = desc.getBuildSpec();
		// get the build commands already associated with project.
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(Builder.BUILDER_ID)) {
				return; // Do nothing if Shadow builder is already associated with project
			}
		}

		ICommand[] newCommands = new ICommand[commands.length + 1];
		// create a new build command
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(Builder.BUILDER_ID); // attach it to the builder
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null); // write to .project file
	}

	@Override
	public void deconfigure() throws CoreException {
		IProjectDescription desc = project.getDescription(); 		 
		ICommand[] commands = desc.getBuildSpec();
		int index = -1;
		// get the build commands already associated with project.
		for (int i = 0; i < commands.length && index == -1; ++i) {
			if (commands[i].getBuilderName().equals(Builder.BUILDER_ID))
				index = i;			
		}

		if( index != -1 ) {
			ICommand[] newCommands = new ICommand[commands.length - 1];
			// create a new build command
			int i;
			for( i = 0; i < index; ++i )
				newCommands[i] = commands[i];
			for( ; i < newCommands.length; ++i )
				newCommands[i] = commands[i + 1];
			ICommand command = desc.newCommand();
			command.setBuilderName(Builder.BUILDER_ID); // attach it to the builder		
			desc.setBuildSpec(newCommands);
			project.setDescription(desc, null); // write to .project file
		}
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}
}
