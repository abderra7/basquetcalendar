import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BasquetCalendar {
	
	public static void main(String[] args) throws IOException {
		if(args.length!=3){
			System.out.println("3 params are mandatory");
			System.out.println("First param basquet catala all jornadas link");
			System.out.println("Second param your team name");
			System.out.println("Third param your adress email");

			return;
		}
        Document doc = Jsoup.connect(args[0]).userAgent("Mozilla").get();;

		Elements jornadas = doc.select("table#resultats tr");
		List<BasquetCalendar.Partit> newPartits = new LinkedList<BasquetCalendar.Partit>();
		int i = 0;
		for(Element partidos : jornadas){
			if(partidos.text().contains(args[1])){
				i++;
				Elements bisbiMatch = partidos.select("td"); 
				BasquetCalendar t = new BasquetCalendar();
				BasquetCalendar.Partit match = t.new Partit();
				int infoNum = 0;
				for(Element info : bisbiMatch){
					match.setNumJornada(i);
					if (infoNum == 0){
						match.setFecha(info.text());
					} else if (infoNum == 1){
						match.setHora(info.text());
					} else if (infoNum == 2){
						match.setLocal(info.text());
					} else if (infoNum == 3){
						match.setVisitante(info.text());
					}
					infoNum++;
				}
				newPartits.add(match);
			}	
		}
		List<BasquetCalendar.Partit> oldPartits = new LinkedList<BasquetCalendar.Partit>();
		boolean updateFile = false;

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("/tmp/filename.txt")));
			for(String line; (line = br.readLine()) != null; ) {
				BasquetCalendar t = new BasquetCalendar();
				BasquetCalendar.Partit match = t.new Partit();
				int infoNum = 0;

		    	for(String info :line.split("###")){
					if (infoNum == 0){
						match.setNumJornada(Integer.valueOf(info));
					} else if (infoNum == 1){
						match.setFecha(info);
					} else if (infoNum == 2){
						match.setHora(info);
					} else if (infoNum == 3){
						match.setLocal(info);
					} else if (infoNum == 4){
						match.setVisitante(info);
					}
					infoNum++;
		    	}
		    	oldPartits.add(match);
		    }
		    // line is not visible here.
		}catch(Exception e){
			updateFile = true;
		}
		
		if(!updateFile){
			i = 0;
			
			while (i<30){
				if(!oldPartits.get(i).getFecha().equals(newPartits.get(i).getFecha())
						|| !oldPartits.get(i).getHora().equals(newPartits.get(i).getHora())){
					updateFile = true;
					String subject, body;
	
					subject = "Basquet - Cambio en la jornada " + oldPartits.get(i).getNumJornada();
	
					body = "Buenas!! <br/><br/> El partido de la jornada "+  oldPartits.get(i).getNumJornada() + ": "
							+ oldPartits.get(i).getLocal() + "-" + oldPartits.get(i).getVisitante()
					+" ha cambiado su horario <br/><br/> "
					+ " Antes era " + oldPartits.get(i).getFecha() + " " + oldPartits.get(i).getHora() +
					" <br/><br/> ahora es " + newPartits.get(i).getFecha() + " " + newPartits.get(i).getHora() 
							+ " <br/><br/>";
					
					sendEmail(subject, body, args[2]);

				}
				i++;
			}
		}
		
		if(updateFile){
			PrintWriter writer = new PrintWriter("/tmp/filename.txt", "UTF-8");
			for(Partit p : newPartits){
				writer.println(p.toString());
			}
			writer.close();
		}
	}
	
	class Partit {
		int numJornada;
		String fecha;
		String local;
		String visitante;
		String hora;
		public String getFecha() {
			return fecha;
		}
		public void setFecha(String fecha) {
			this.fecha = fecha;
		}
		public String getLocal() {
			return local;
		}
		public void setLocal(String local) {
			this.local = local;
		}
		public String getVisitante() {
			return visitante;
		}
		public void setVisitante(String visitante) {
			this.visitante = visitante;
		}
		public String getHora() {
			return hora;
		}
		public void setHora(String hora) {
			this.hora = hora;
		}
		public int getNumJornada() {
			return numJornada;
		}
		public void setNumJornada(int numJornada) {
			this.numJornada = numJornada;
		}
		@Override
		public String toString() {
			return numJornada + "###" + fecha + "###" + hora + "###"
					+ local + "###" + visitante;
		}
	}
	
	public static void sendEmail(String subject, String body, String email) {
	      // Recipient's email ID needs to be mentioned.
	      String to = email;

	      // Sender's email ID needs to be mentioned
	      String from = email;

	      // Assuming you are sending email from localhost
	      String host = "localhost";

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);

	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(properties);

	      try {
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

	         // Set Subject: header field
	         message.setSubject(subject);

	         // Send the actual HTML message, as big as you like
	         message.setContent(body, "text/html");

	         // Send message
	         Transport.send(message);
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	   }
	
}
