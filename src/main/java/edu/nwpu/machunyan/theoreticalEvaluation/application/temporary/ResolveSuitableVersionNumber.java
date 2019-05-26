package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationJam;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.FaultLocationLoader;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;

/**
 * 找到能用于统计中的版本的数量（错误类型不包括缺少语句）
 */
public class ResolveSuitableVersionNumber {
    public static void main(String[] args) {

        LogUtils.logInfo("program,all,used");

        for (String programName : ProgramDefination.PROGRAM_LIST) {

            final FaultLocationJam faultLocationJam = FaultLocationLoader.getFaultLocations(programName).get();

            final int all = faultLocationJam.getFaultLocationForPrograms().size();
            final long used = faultLocationJam
                .getFaultLocationForPrograms()
                .stream()
                .filter(FaultLocationForProgram::isUsedInEffectSize)
                .count();

            LogUtils.logInfo(programName + "," + all + "," + used);
        }
    }
}
