package com.phonestore.repository;

import com.phonestore.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /** Tìm gần đúng theo tên khách hàng hoặc ngày (yyyy-MM-dd hoặc dd/MM/yyyy). */
    @Query(value = """
            SELECT i.* FROM invoice i JOIN customer c ON i.customer_id = c.id
            WHERE :kw = ''
               OR c.name LIKE CONCAT('%', :kw, '%')
               OR DATE_FORMAT(i.created_at, '%Y-%m-%d') LIKE CONCAT('%', :kw, '%')
               OR DATE_FORMAT(i.created_at, '%d/%m/%Y') LIKE CONCAT('%', :kw, '%')
            ORDER BY i.created_at DESC, i.id DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM invoice i JOIN customer c ON i.customer_id = c.id
            WHERE :kw = ''
               OR c.name LIKE CONCAT('%', :kw, '%')
               OR DATE_FORMAT(i.created_at, '%Y-%m-%d') LIKE CONCAT('%', :kw, '%')
               OR DATE_FORMAT(i.created_at, '%d/%m/%Y') LIKE CONCAT('%', :kw, '%')
            """,
            nativeQuery = true)
    Page<Invoice> search(@Param("kw") String kw, Pageable pageable);

    // Doanh thu: bỏ qua đơn đã huỷ
    @Query(value = """
            SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS period, SUM(total) AS revenue, COUNT(*) AS orders
            FROM invoice WHERE status <> 'CANCELLED'
            GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') ORDER BY period DESC
            """, nativeQuery = true)
    List<RevenueView> revenueByDay();

    @Query(value = """
            SELECT DATE_FORMAT(created_at, '%Y-%m') AS period, SUM(total) AS revenue, COUNT(*) AS orders
            FROM invoice WHERE status <> 'CANCELLED'
            GROUP BY DATE_FORMAT(created_at, '%Y-%m') ORDER BY period DESC
            """, nativeQuery = true)
    List<RevenueView> revenueByMonth();

    @Query(value = """
            SELECT DATE_FORMAT(created_at, '%Y') AS period, SUM(total) AS revenue, COUNT(*) AS orders
            FROM invoice WHERE status <> 'CANCELLED'
            GROUP BY DATE_FORMAT(created_at, '%Y') ORDER BY period DESC
            """, nativeQuery = true)
    List<RevenueView> revenueByYear();
}
