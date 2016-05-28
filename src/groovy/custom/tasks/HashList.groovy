package groovy.custom.tasks

import groovy.json.JsonOutput
import groovyjarjarcommonscli.MissingArgumentException
import org.apache.tools.ant.Project
import org.apache.tools.ant.Task
import org.apache.tools.ant.types.FileSet

import java.security.MessageDigest;

/**
 * Created by Paulina Sadowska on 28.05.2016.
 */
class HashList extends Task {

    private String defaultHashesPropertyName = "hashlist.default_hashes";
    String fileName = "hashlistOutput.json";
    String checksumTypes;

    private List<FileSet> fileSets = new ArrayList<>();

    public void execute() {

        if (fileSets.isEmpty())
            throw new MissingArgumentException("Fileset don't exists");

        Project project = getProject();
        if (!checksumTypes?.trim()) {
            if (project.getProperties()[defaultHashesPropertyName] == null)
                throw new MissingPropertyException("${defaultHashesPropertyName} not found");
            checksumTypes = project.getProperties()[defaultHashesPropertyName];
        }

        def checksumList = checksumTypes.tokenize(",").collect { it.trim() }
        def outputCollection = new ArrayList();

        fileSets.collect{
            def currentDirectoryScanner = it.getDirectoryScanner(project);
            outputCollection.add("files": currentDirectoryScanner.getIncludedFiles()
                    .collect {
                filePath ->
                    def file = new File("${currentDirectoryScanner.getBasedir().absolutePath}/${filePath}");
                    def output = ["path": filePath, "size": file.length(), modified: new Date(file.lastModified()).format("yyyy-MM-dd HH:mm:ss")]
                    checksumList.each {
                        checksum ->
                            def value = MessageDigest.getInstance(checksum);
                            output[checksum] = value.digest(file.readBytes()).encodeHex().toString();
                    };
                    output;
            });
        }
        def text = JsonOutput.prettyPrint(JsonOutput.toJson(outputCollection))
        def outputFile = new File("${project.baseDir}/${fileName}");
        outputFile.write(text);
    }

    public void addFileset(FileSet fileSet) {
        fileSets.add(fileSet);
    }
}
