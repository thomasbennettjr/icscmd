import com.metaopsis.icscmd.icscmd;
import org.junit.Test;

/**
 * Created by tbennett on 11/9/16.
 */
public class ICSCMDTests {

    @Test
    public void TestAPINoWait()
    {
        String[] args = {"-un", "tbennett@criticalmindsad.com", "-pw", "lak3v13w.c0m", "-j", "mct_iot_load_infractions", "-t", "MTT"};
        icscmd.main(args);

    }

    @Test
    public void TestAPIWait()
    {
        String[] args = {"-un", "tbennett@unicosolution.com", "-pw", "YElpc1plT0N9SyFkZ1MsdTd3cS9vNm9tb3dlY3NjWmtueXcwaWc9PQ==", "-j", "mct_Tom_CentOS", "-t", "MTT", "-w"};
        icscmd.main(args);

    }

    @Test
    public void TestAPIStopJob()
    {
        String[] args = {"-un", "tbennett@criticalmindsad.com", "-pw", "lak3v13w.c0m", "-j", "mct_iot_load_infractions", "-t", "MTT", "-s"};
        icscmd.main(args);

    }

    @Test
    public void TestCliTypes()
    {
        String[] args = {"-un", "tbennett@criticalmindsad.com", "-pw", "lak3v13w.c0m", "-j", "mct_iot_load_infractions", "-t", "MTQ", "-s"};
        icscmd.main(args);

    }

    @Test
    public void TestCliStopandWaitMix()
    {
        String[] args = {"-un", "tbennett@criticalmindsad.com", "-pw", "lak3v13w.c0m", "-j", "mct_iot_load_infractions", "-t", "MTT", "-s", "-w"};
        icscmd.main(args);

    }

    @Test
    public void TestCliJustHelp()
    {
        String[] args = {"-h"};
        icscmd.main(args);

    }
}
