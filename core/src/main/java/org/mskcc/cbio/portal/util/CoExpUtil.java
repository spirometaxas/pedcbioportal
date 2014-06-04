package org.mskcc.cbio.portal.util;

import java.util.*;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.dao.*;



public class CoExpUtil {

    public static ArrayList<String> getCaseIds(String caseSetId, String caseIdsKey) {
		try {
			DaoCaseList daoCaseList = new DaoCaseList();
            CaseList caseList;
            ArrayList<String> caseIdList = new ArrayList<String>();
            if (caseSetId.equals("-1")) {
                String strCaseIds = CaseSetUtil.getCaseIds(caseIdsKey);
                String[] caseArray = strCaseIds.split("\\s+");
                for (String item : caseArray) {
                    caseIdList.add(item);
                }
            } else {
                caseList = daoCaseList.getCaseListByStableId(caseSetId);
                caseIdList = caseList.getCaseList();
            }
			return caseIdList;
        } catch (DaoException e) {
            System.out.println("Caught Dao Exception: " + e.getMessage());
			return null;
        }
    }

	public static GeneticProfile getPreferedGeneticProfile(String cancerStudyIdentifier) {
        CancerStudy cs = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
        ArrayList<GeneticProfile> gps = DaoGeneticProfile.getAllGeneticProfiles(cs.getInternalId());
        GeneticProfile final_gp = null;
        for (GeneticProfile gp : gps) {
            // TODO: support miRNA later
            if (gp.getGeneticAlterationType() == GeneticAlterationType.MRNA_EXPRESSION) {
                //rna seq profile (no z-scores applied) holds the highest priority)
                if (gp.getStableId().toLowerCase().contains("rna_seq") &&
                   !gp.getStableId().toLowerCase().contains("zscores")) {
                    final_gp = gp;
                    break;
                } else if (!gp.getStableId().toLowerCase().contains("zscores")) {
                    final_gp = gp;
                }
            }
        }
        return final_gp;
    }

    public static Map<Long,double[]> getExpressionMap(int profileId, String caseSetId, String caseIdsKey) throws DaoException {
        //Filter out cases with no values
        ArrayList<String> caseIds = getCaseIds(caseSetId, caseIdsKey);
        caseIds.retainAll(DaoCaseProfile.getAllCaseIdsInProfile(profileId));

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();

        Map<Long, HashMap<String, String>> mapStr = daoGeneticAlteration.getGeneticAlterationMap(profileId, null);
        Map<Long, double[]> map = new HashMap<Long, double[]>(mapStr.size());
        for (Map.Entry<Long, HashMap<String, String>> entry : mapStr.entrySet()) {
            Long gene = entry.getKey();
            Map<String, String> mapCaseValueStr = entry.getValue();
            double[] values = new double[caseIds.size()];
            boolean isValid = true;
            for (int i = 0; i < caseIds.size(); i++) {
                String caseId = caseIds.get(i);
                String value = mapCaseValueStr.get(caseId);
                Double d;
                try {
                    d = Double.valueOf(value);
                } catch (Exception e) {
                    d = Double.NaN;
                }
                // if (d!=null && !d.isNaN()) {
                     values[i]=d;
                // } else {
                //     isValid = false;
                //     break;
                // }
            }
            // if (isValid) {
                 map.put(gene, values);
            // }
        }
        return map;
    }
	
}
