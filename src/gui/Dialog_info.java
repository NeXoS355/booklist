package gui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;

import javax.swing.JDialog;
import javax.swing.JLabel;

import application.BookListModel;

public class Dialog_info extends JDialog {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Dialog Info Constructor
	 * 
	 * @param owner - set the owner of this Frame
	 */
	public Dialog_info(Frame owner) {

		this.setTitle(Localization.get("t.info"));
		this.setModal(true);
		this.setLayout(new GridBagLayout());
		this.setLocationRelativeTo(owner);

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.ipady = 5;
		c.insets = new Insets(10, 10, 0, 10);
		JLabel lblHeader = new JLabel(Localization.get("t.info"));
		lblHeader.setFont(Mainframe.defaultFont);
		this.add(lblHeader, c);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.05;
		c.gridwidth = 1;
		JLabel lbl_DescMostReadAuthor = new JLabel(Localization.get("info.mostAuthor"));
		this.add(lbl_DescMostReadAuthor, c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = 1;
		JLabel lbl_mostReadAuthor = new JLabel();
		lbl_mostReadAuthor.setText(BookListModel.getMostOf("autor").toString().replace("[", "").replace("]", ""));
		this.add(lbl_mostReadAuthor, c);
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.05;
		c.gridwidth = 1;
		JLabel lbl_DescMostReadSeries = new JLabel(Localization.get("info.mostSeries"));
		this.add(lbl_DescMostReadSeries, c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 0.5;
		c.gridwidth = 1;
		JLabel lbl_mostReadSeries = new JLabel();
		lbl_mostReadSeries.setText(BookListModel.getMostOf("serie").toString().replace("[", "").replace("]", ""));
		this.add(lbl_mostReadSeries, c);
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.05;
		c.gridwidth = 1;
		JLabel lbl_DescBestAuthor = new JLabel(Localization.get("info.bestAuthor"));
		this.add(lbl_DescBestAuthor, c);
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 0.5;
		c.gridwidth = 1;
		JLabel lbl_BestAuthor = new JLabel();
		lbl_BestAuthor.setText(BookListModel.getBestRatingOf("autor").toString().replace("[", "").replace("]", ""));
		this.add(lbl_BestAuthor, c);
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.05;
		c.gridwidth = 1;
		JLabel lbl_DescBestSeries = new JLabel(Localization.get("info.bestSeries"));
		this.add(lbl_DescBestSeries, c);
		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 0.5;
		c.gridwidth = 1;
		JLabel lbl_BestSeries = new JLabel();
		lbl_BestSeries.setText(BookListModel.getBestRatingOf("serie").toString().replace("[", "").replace("]", ""));
		this.add(lbl_BestSeries, c);
		c.gridx = 0;
		c.gridy = 5;
		c.weightx = 0.05;
		c.gridwidth = 1;
		JLabel lbl_DescEbook = new JLabel(Localization.get("info.countEbooks"));
		this.add(lbl_DescEbook, c);
		c.gridx = 1;
		c.gridy = 5;
		c.weightx = 0.5;
		c.gridwidth = 1;
		JLabel lbl_Ebooks = new JLabel();
		lbl_Ebooks.setText(Integer.toString(BookListModel.getEbookCount(1)));
		this.add(lbl_Ebooks, c);
		c.gridx = 0;
		c.gridy = 6;
		c.weightx = 0.05;
		c.gridwidth = 1;
		JLabel lbl_DescNotEbook = new JLabel(Localization.get("info.countBooks"));
		this.add(lbl_DescNotEbook, c);
		c.gridx = 1;
		c.gridy = 6;
		c.weightx = 0.5;
		c.gridwidth = 1;
		JLabel lbl_NotEbooks = new JLabel();
		lbl_NotEbooks.setText(Integer.toString(BookListModel.getEbookCount(0)));
		this.add(lbl_NotEbooks, c);
		c.gridx = 0;
		c.gridy = 7;
		c.weightx = 0.05;
		c.gridwidth = 1;
		JLabel lbl_DescYear = new JLabel(Localization.get("info.countBooksPerYear"));
		this.add(lbl_DescYear, c);
		c.gridx = 1;
		c.gridy = 7;
		c.weightx = 0.5;
		c.gridwidth = 1;
		JLabel lbl_Year = new JLabel();
		String bookOverview = BookListModel.getBooksPerYear();
		int countYears = bookOverview.split("<br>").length;
		lbl_Year.setText(BookListModel.getBooksPerYear());
		this.add(lbl_Year, c);
		
		this.setSize(400, 175+28*countYears);
		this.setVisible(true);
	}

}
