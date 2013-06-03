/**
 * 
 */
package org.jboss.tools.livereload.internal;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.ide.filesystem.FileSystemStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xcoulon
 * 
 */
@SuppressWarnings("restriction")
public class WorkbenchUtils {

	static final Logger LOGGER = LoggerFactory.getLogger(WorkbenchUtils.class);

	public static String retrieveSampleProjectName(Class<?> clazz) {
		RunWithProject annotation = clazz.getAnnotation(RunWithProject.class);
		while (annotation == null && clazz.getSuperclass() != null) {
			clazz = clazz.getSuperclass();
			annotation = clazz.getAnnotation(RunWithProject.class);
		}
		Assert.assertNotNull("Unable to locate @RunWithProject annotation",
				annotation);
		return annotation.value();

	}

	public static void copyFile(IPath projectSourcePath, IResource resource,
			IWorkspace targetWorkspace, IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		List<File> filesToImport = new ArrayList<File>(1);
		File file = projectSourcePath.append(resource.getProjectRelativePath())
				.toFile();
		if (!file.exists()) {
			return;
		}
		filesToImport.add(file);
		IPath resourcePath = resource.getProjectRelativePath()
				.removeLastSegments(1);
		IPath containerPath = null;
		if (resourcePath.segmentCount() > 0) {
			containerPath = resource.getProject().getFolder(resourcePath)
					.getFullPath();
		} else {
			containerPath = resource.getProject().getFullPath();
		}
		ImportOperation operation = new ImportOperation(containerPath,
				new FileSystemStructureProvider(), new IOverwriteQuery() {
					@Override
					public String queryOverwrite(String pathString) {
						return IOverwriteQuery.YES;
					}
				}, filesToImport);
		operation.setContext(null);
		// need to overwrite modified files
		operation.setOverwriteResources(true);
		operation.setCreateContainerStructure(false);
		LOGGER.debug("Copying {} into {}", file.getAbsolutePath(), resource
				.getProject().getLocation());
		operation.run(monitor);
	}

	public static IPath getSampleProjectPath(String projectName) {
		IPath path = null;
		if (System.getProperty("user.dir") != null) {
			path = new Path(System.getProperty("user.dir")).append("projects")
					.append(projectName).makeAbsolute();
		} else {
			Assert.fail("The sample project was not found in the launcher workspace under name '"
					+ projectName + "'");

		}
		LOGGER.debug(projectName + " path=" + path.toOSString());
		return path;
	}

	/**
	 * Called by subclasses to setup the workspace with project and files (xml,
	 * java, etc.)
	 * 
	 * @param projectName
	 * 
	 * @throws Exception
	 */
	public static IProject syncSampleProject(String projectName)
			throws Exception {
		AbstractCommonTestCase.LOGGER.debug("Sync'ing sample project "
				+ projectName);
		IWorkspace junitWorkspace = ResourcesPlugin.getWorkspace();
		IPath projectSourcePath = WorkbenchUtils
				.getSampleProjectPath(projectName);
		Long start = new Date().getTime();
		NullProgressMonitor monitor = new NullProgressMonitor();
		syncProject(projectSourcePath, junitWorkspace, monitor);
		// TODO : wait until processes are finished
		AbstractCommonTestCase.LOGGER.debug("Sync'ing sample project done in "
				+ (new Date().getTime() - start) + " millis");
		junitWorkspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		IProject project = junitWorkspace.getRoot().getProject(projectName);
		project.open(monitor);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		return project;
	}

	/**
	 * Synchronize the target project with the path given in parameter.
	 * 
	 * @param sourcePath
	 * @param monitor
	 * @return true if ImportOperation to synchronize was performed (ie, was
	 *         required), false otherwise.
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 * @throws CoreException
	 */
	// FIXME : when samples sources are modified in the dev workspace, they are
	// not published in the junit-workspace...
	public static void syncProject(IPath projectSourcePath,
			IWorkspace targetWorkspace, IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException,
			CoreException {
		IProject project = getTargetWorkspaceProject(projectSourcePath,
				targetWorkspace, monitor);
		LOGGER.info("Removing added files from the target workspace");
		// reverse detection operation
		SyncFileSystemStructureProvider syncFileSystemStructureProvider = new SyncFileSystemStructureProvider.Builder(
				project.getLocation(), projectSourcePath)
				.ignoreRelativeSourcePaths("target", "bin", ".svn", ".git",
						".project", ".classpath", ".settings").build();
		List<File> filesToRemove = syncFileSystemStructureProvider
				.getChildren(project.getLocation().toFile());
		for (File fileToRemove : filesToRemove) {
			Assert.assertTrue("File not deleted : " + fileToRemove,
					fileToRemove.delete());
		}
		LOGGER.info("adding missing or modified files in the target workspace...");
		LOGGER.info("Source path: " + projectSourcePath.toPortableString());
		LOGGER.info("Target workspace location: "
				+ targetWorkspace.getRoot().getRawLocation());
		 syncFileSystemStructureProvider = new SyncFileSystemStructureProvider.Builder(
				projectSourcePath, project.getLocation()).ignoreRelativeSourcePaths(".svn", ".git", "target", "bin").build();
		List<File> filesToImport = syncFileSystemStructureProvider
				.getChildren(projectSourcePath.toFile());
		if (filesToImport != null && filesToImport.size() > 0) {
			ImportOperation operation = new ImportOperation(
					project.getFullPath(), projectSourcePath.toFile(),
					syncFileSystemStructureProvider, new IOverwriteQuery() {
						@Override
						public String queryOverwrite(String pathString) {
							return IOverwriteQuery.YES;
						}
					}, filesToImport);
			operation.setContext(null);
			// need to overwrite modified files
			operation.setOverwriteResources(true);
			operation.setCreateContainerStructure(false);
			operation.run(monitor);
		}
		
		buildProject(project, monitor);
	}

	public static void buildProject(final IProject project,
			final IProgressMonitor progressMonitor) throws CoreException,
			OperationCanceledException, InterruptedException {
		buildProject(project, IncrementalProjectBuilder.FULL_BUILD,
				progressMonitor);
	}

	public static void buildProject(final IProject project,
			final int buildKind, final IProgressMonitor progressMonitor)
			throws CoreException, OperationCanceledException,
			InterruptedException {
		project.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
		project.build(buildKind, progressMonitor);
		Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
	}

	/**
	 * @param sourceWorkspace
	 * @param targetWorkspace
	 * @param path
	 * @param monitor
	 * @return
	 * @throws CoreException
	 * @throws InvocationTargetException
	 */
	public static IProject getTargetWorkspaceProject(IPath projectSourcePath,
			IWorkspace targetWorkspace, IProgressMonitor monitor)
			throws CoreException, InvocationTargetException {
		IPath dotProjectPath = projectSourcePath.addTrailingSeparator().append(
				".project");
		IProjectDescription description = targetWorkspace
				.loadProjectDescription(dotProjectPath);
		String projectName = description.getName();
		IProject project = targetWorkspace.getRoot().getProject(projectName);
		if (project.exists()
				&& !targetWorkspace.getRoot()
						.getFile(project.getFile(".project").getFullPath())
						.exists()) {
			LOGGER.warn("Deleting (with force!) the project as it seems to be in an invalid state...");
			project.delete(true, monitor);
		} else if (project.exists() && !project.isOpen()) {
			project.open(monitor);
		} else  
		if (!project.exists()) {
			createProject(monitor, description, projectName, targetWorkspace,
					project);
		}
		
		return project;
	}

	/**
	 * @param monitor
	 * @param description
	 * @param projectName
	 * @param workspace
	 * @param project
	 * @throws InvocationTargetException
	 */
	static void createProject(IProgressMonitor monitor,
			IProjectDescription description, String projectName,
			IWorkspace workspace, IProject project)
			throws InvocationTargetException {
		// import from file system

		// import project from location copying files - use default project
		// location for this workspace
		// if location is null, project already exists in this location or
		// some error condition occured.
		IProjectDescription desc = workspace.newProjectDescription(projectName);
		desc.setBuildSpec(description.getBuildSpec());
		desc.setComment(description.getComment());
		desc.setDynamicReferences(description.getDynamicReferences());
		desc.setNatureIds(description.getNatureIds());
		desc.setReferencedProjects(description.getReferencedProjects());
		description = desc;

		try {
			monitor.beginTask(
					DataTransferMessages.WizardProjectsImportPage_CreateProjectsTask,
					100);
			project.create(description, new SubProgressMonitor(monitor, 30));
			project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(
					monitor, 70));
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Replace the first occurrence of the given old content with the new content. Fails if the old content is not found
	 * (avoids weird side effects in the rest of the test).
	 * 
	 * @param compilationUnit
	 * @param oldContent
	 * @param newContent
	 * @param useWorkingCopy
	 * @return 
	 * @throws CoreException 
	 * @throws IOException 
	 */
	public static IFile replaceAllOccurrencesOfCode(final String fileName, final IProject project, final String oldContent,
			final String newContent) throws IOException, CoreException {
		final IResource resource = project.findMember(fileName);
		if(resource.getType() == IResource.FILE) {
			final IFile file = (IFile) resource;
			final String buffer = IOUtils.toString(file.getContents());
			int offset = buffer.indexOf(oldContent);
			assertTrue("Old content '" + oldContent + "' not found", offset != -1);
			file.setContents(IOUtils.toInputStream(buffer.replace(oldContent, newContent)), IResource.FORCE, null);
			project.build(IncrementalProjectBuilder.AUTO_BUILD, null);
			return file;
		} else {
			fail("Resource location '" + fileName + "' does not match an existing file in project '" + project.getName() + "'");
			return null;
		}
	}
	

}
