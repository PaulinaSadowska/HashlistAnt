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
class HashList extends Task{

    private String defaultHashesPropertyName = "hashlist.default_hashes";
    String file = "hashlistOutput.json";
    String checksums;

    private List<FileSet> fileSets = new ArrayList<>();

    public void execute() {

        Project project = getProject();
        if(!checksums?.trim ()) {
            if(project.getProperties()[defaultHashesPropertyName] == null)
                throw new MissingPropertyException("${defaultHashesPropertyName} not found");
            checksums = project.getProperties()[defaultHashesPropertyName];
        }

        if(fileSets.isEmpty())
            throw new MissingArgumentException("Fileset don't exists");

        def outputCollection = new ArrayList();

        for(fileset in fileSets){
            def currentDirectoryScanner = fileset.getDirectoryScanner(project);
            outputCollection.add("files" : currentDirectoryScanner.getIncludedFiles().collect{
                filePath ->
                    def file = new File("${currentDirectoryScanner.getBasedir().absolutePath}/${filePath}");
                    def output = ["path": filePath, "size": file.length(), modified: new Date(file.lastModified()).format("yyyy-MM-dd HH:mm:ss")]
                    checksums.tokenize(",")
                            .collect {it.trim()}
                            .each{
                        checksum ->
                            def value = MessageDigest.getInstance(checksum);
                            output[checksum] = value.digest(file.readBytes()).encodeHex().toString();
                    };
                    output;
            });
        }
        if(outputCollection.size()>0) {
            def text = JsonOutput.prettyPrint(JsonOutput.toJson(outputCollection))
            def outputFile = new File("${project.baseDir}/${file}");
            outputFile.write(text);
        }
    }


    public void addFileset(FileSet fileSet)
    {
        fileSets.add(fileSet);
    }
}
