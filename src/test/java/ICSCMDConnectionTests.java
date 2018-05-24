import com.metaopsis.icscmd.icscmd;
import org.junit.Test;

/**
 * Created by tombennett on 8/19/17.
 */
public class ICSCMDConnectionTests
{
    @Test
    public void TestBulkChange()
    {
        String[] args = {"-un", "tbennett@unicosolution.app.com", "-pw", "RCl2eWJObDl7dS4rcjF6QVhkcDNFV2x2bjNvZ1BNREl5WFBjY1E9PQ==", "-c", "-csf", "/Users/tombennett/IdeaProjects/icscmd/testit.txt"};
        icscmd.main(args);
    }
}

