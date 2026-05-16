package regulation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.OracleConnection;
import exception.ErrorCode;
import exception.SystemException;

public class RegulationDAO {

	public RegulationDTO getRegulationByCategoryId(int categoryId) {
		String sql = "SELECT regulationId, categoryId, limitCapacity, establishedDate, overageRate "
				+ "FROM Regulation WHERE categoryId = ?";

		try (Connection conn = OracleConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, categoryId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (SQLException e) {
			throw new SystemException(ErrorCode.DB_CONNECTION, e);
		}
		return null;
	}

	private RegulationDTO mapRow(ResultSet rs) throws SQLException {
		return RegulationDTO.builder()
				.regulationId(rs.getInt("regulationId"))
				.categoryId(rs.getInt("categoryId"))
				.limitCapacity(rs.getInt("limitCapacity"))
				.establishedDate(rs.getDate("establishedDate").toLocalDate())
				.overageRate(rs.getInt("overageRate"))
				.build();
	}
}
