package test.custom.tasks

import org.apache.tools.ant.BuildFileRule
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created by Paulina Sadowska on 28.05.2016.
 */
class HashListTest {

    public final BuildFileRule buildFileRule = new BuildFileRule();

    @Before
    public void SetUp()
    {
        buildFileRule.configureProject("build.xml");
    }

    @After
    public void Destroy()
    {
        def outputDir = buildFileRule.getProject()?.baseDir;
        outputDir.listFiles().findAll { it.name.contains(".json")}.collect { it.delete()};
    }

    @Test
    public void TestExecute()
    {
        def targetName = "hashlist-example";
        buildFileRule.executeTarget(targetName);
    }

}
