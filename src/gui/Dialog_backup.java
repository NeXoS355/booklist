package gui;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.util.UIScale;

public class Dialog_backup extends JDialog {

	@Serial
	private static final long serialVersionUID = 1L;

	private final JPanel listPanel;

	public Dialog_backup(Frame owner) {
		setTitle(Localization.get("backup.manage.title"));
		setModal(true);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(new EmptyBorder(UIScale.scale(20), UIScale.scale(24), UIScale.scale(20), UIScale.scale(24)));

		// Header
		JLabel header = new JLabel(Localization.get("backup.manage.title"));
		header.setFont(Mainframe.defaultFont.deriveFont(Font.BOLD, Mainframe.defaultFont.getSize() * 1.4f));
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(header);
		content.add(Box.createVerticalStrut(UIScale.scale(12)));

		// New backup button (oben)
		JButton btnNewBackup = new JButton(Localization.get("backup.manage.newBackup"));
		btnNewBackup.setAlignmentX(Component.LEFT_ALIGNMENT);
		btnNewBackup.addActionListener(e -> {
			boolean success = Mainframe.createBackup();
			if (success) {
				Mainframe.showNotification(Localization.get("backup.success"));
				refreshList();
			} else {
				JOptionPane.showMessageDialog(this, Localization.get("backup.error"));
			}
		});
		content.add(btnNewBackup);
		content.add(Box.createVerticalStrut(UIScale.scale(16)));

		// Backup list
		listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		refreshList();

		content.add(listPanel);

		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(UIScale.scale(16));
		setContentPane(scrollPane);

		setSize(UIScale.scale(600), UIScale.scale(560));
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	private void refreshList() {
		listPanel.removeAll();

		String workingDir = System.getProperty("user.dir");
		File backupNew = new File(workingDir, "Backup");
		File backupOld = new File(workingDir, "Sicherung");

		java.util.List<File> allBackups = new java.util.ArrayList<>();
		if (backupNew.exists() && backupNew.isDirectory()) {
			File[] dirs = backupNew.listFiles(File::isDirectory);
			if (dirs != null) allBackups.addAll(Arrays.asList(dirs));
		}
		if (backupOld.exists() && backupOld.isDirectory()) {
			File[] dirs = backupOld.listFiles(File::isDirectory);
			if (dirs != null) allBackups.addAll(Arrays.asList(dirs));
		}

		if (allBackups.isEmpty()) {
			JLabel noBackups = new JLabel(Localization.get("backup.manage.noBackups"));
			noBackups.setForeground(UIManager.getColor("Label.disabledForeground"));
			noBackups.setAlignmentX(Component.LEFT_ALIGNMENT);
			listPanel.add(noBackups);
			listPanel.revalidate();
			listPanel.repaint();
			return;
		}

		File[] backupDirs = allBackups.toArray(new File[0]);

		// Sort by epoch descending (newest first)
		Arrays.sort(backupDirs, Comparator.comparing(File::getName).reversed());

		for (File dir : backupDirs) {
			listPanel.add(createBackupCard(dir));
			listPanel.add(Box.createVerticalStrut(UIScale.scale(8)));
		}

		listPanel.revalidate();
		listPanel.repaint();
	}

	private JPanel createBackupCard(File backupDir) {
		JPanel card = new JPanel(new BorderLayout(UIScale.scale(12), 0));
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"), 1),
				new EmptyBorder(UIScale.scale(10), UIScale.scale(14), UIScale.scale(10), UIScale.scale(14))
		));
		card.setBackground(UIManager.getColor("Panel.background"));
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(80)));

		// Left: info
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.setOpaque(false);

		// Date from epoch folder name
		String dateStr = formatEpochDirName(backupDir.getName());
		JLabel dateLabel = new JLabel(dateStr);
		dateLabel.setFont(Mainframe.defaultFont.deriveFont(Font.BOLD));
		dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(dateLabel);

		// Version + size
		String version = readVersionFromBackup(backupDir);
		long sizeBytes = calculateDirSize(backupDir);
		String sizeMB = String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
		JLabel detailLabel = new JLabel("Version: " + version + "  |  " + sizeMB);
		detailLabel.setFont(Mainframe.defaultFont.deriveFont(Font.PLAIN, Mainframe.defaultFont.getSize() * 0.8f));
		detailLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(detailLabel);

		card.add(infoPanel, BorderLayout.CENTER);

		// Right: buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIScale.scale(6), 0));
		buttonPanel.setOpaque(false);

		JButton btnRestore = new JButton(Localization.get("backup.manage.restore"));
		btnRestore.addActionListener(e -> restoreBackup(backupDir));

		JButton btnDelete = new JButton(Localization.get("backup.manage.delete"));
		btnDelete.addActionListener(e -> deleteBackup(backupDir));

		buttonPanel.add(btnRestore);
		buttonPanel.add(btnDelete);
		card.add(buttonPanel, BorderLayout.EAST);

		return card;
	}

	private String formatEpochDirName(String name) {
		try {
			long epoch = Long.parseLong(name);
			Date date = new Date(epoch * 1000L);
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy  HH:mm");
			return sdf.format(date);
		} catch (NumberFormatException e) {
			return name;
		}
	}

	private String readVersionFromBackup(File backupDir) {
		File configFile = new File(backupDir, "config.conf");
		if (!configFile.exists()) {
			return "unbekannt";
		}
		Properties props = new Properties();
		try (FileReader reader = new FileReader(configFile)) {
			props.load(reader);
			String version = props.getProperty("version");
			return version != null ? version : "unbekannt";
		} catch (IOException e) {
			return "unbekannt";
		}
	}

	private long calculateDirSize(File dir) {
		long size = 0;
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					size += file.length();
				} else if (file.isDirectory()) {
					size += calculateDirSize(file);
				}
			}
		}
		return size;
	}

	private void restoreBackup(File backupDir) {
		String dateStr = formatEpochDirName(backupDir.getName());
		int answer = JOptionPane.showConfirmDialog(this,
				MessageFormat.format(Localization.get("backup.manage.restoreQuestion"), dateStr),
				Localization.get("backup.manage.restore"),
				JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			data.Database.closeConnection();

			Path workingDir = Path.of(System.getProperty("user.dir"));

			File[] backupFiles = backupDir.listFiles();
			if (backupFiles != null) {
				for (File file : backupFiles) {
					Path target = workingDir.resolve(file.getName());
					if (file.isDirectory()) {
						// DerbyDB (BooklistDB/) aus alten Backups
						Mainframe.copyFilesInDirectory(file, target.toFile());
					} else {
						Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}

			JOptionPane.showMessageDialog(this, Localization.get("backup.manage.restoreSuccess"));
		} catch (IOException e) {
			Mainframe.logger.error("Fehler beim Wiederherstellen des Backups: {}", e.getMessage());
			JOptionPane.showMessageDialog(this, Localization.get("backup.manage.restoreError"),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void deleteBackup(File backupDir) {
		String dateStr = formatEpochDirName(backupDir.getName());
		int answer = JOptionPane.showConfirmDialog(this,
				MessageFormat.format(Localization.get("backup.manage.deleteQuestion"), dateStr),
				Localization.get("backup.manage.delete"),
				JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			deleteDirectoryRecursive(backupDir.toPath());
			refreshList();
		} catch (IOException e) {
			Mainframe.logger.error("Fehler beim Löschen des Backups: {}", e.getMessage());
			JOptionPane.showMessageDialog(this, Localization.get("backup.manage.deleteError"),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void deleteDirectoryRecursive(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try (var entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					deleteDirectoryRecursive(entry);
				}
			}
		}
		Files.deleteIfExists(path);
	}
}
