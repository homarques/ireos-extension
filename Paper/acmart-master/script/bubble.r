bubble <- function(){
	dataset  = read.table("/home/henrique/Documents/journal/acmart-master/data/knn-outlier_order.txt", stringsAsFactors=F)
	dataset = dataset[,-1]
	dataset[,4] = as.numeric(substr(dataset[,3],13,100))
	dataset[,3] = "inlier"
	dataset[1:5,3] = "outlier"
	file = "/home/henrique/Documents/journal/acmart-master/figs/bubble.pdf"
	radius <- sqrt( dataset[,3]/ pi )
	pdf(file)
	par(mgp=c(10,10,10))
	par(mar=c(0.2,0.2,0.2,0.2))
	plot(dataset[,1:2], cex=0.8)
	#symbols(dataset[,1], dataset[,2], circles=dataset[,4], inches=max(dataset[,4]), cex=1.5, xlim=c(-0,0.3), ylim=c(0.5,0.8))
	#symbols(dataset[which(dataset[,3]=="outlier"),1], dataset[which(dataset[,3]=="outlier"),2], circles=dataset[which(dataset[,3]=="outlier"),4], inches=max(dataset[,4]), fg="red", add=T, cex=1.5)
	#text(dataset[which(dataset[,3]=="outlier"),1], dataset[which(dataset[,3]=="outlier"),2], round(dataset[which(dataset[,3]=="outlier"),4], dig=2), cex=3)
	points(dataset[which(dataset[,3]=="outlier"),1], dataset[which(dataset[,3]=="outlier"),2], col=2, pch=15, cex=1.5)
	dev.off()
	
}


plotData <- function(){
		data = read.table("/home/henrique/Documents/journal/acmart-master/data/ilx3")
		seps = read.table("/home/henrique/Documents/journal/acmart-master/data/mahalanobis")
		file = "/home/henrique/Documents/journal/acmart-master/figs/data.pdf"
		pdf(file)
		par(mgp=c(10,10,10))
		par(mar=c(4,4,4,4))
		plot(data, cex=0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		points(data[101,], col = "white", cex = 0.8)
		points(data[102,], col = "white", cex = 0.8)
		points(data[99,], col = "white", cex = 0.8)
		
		points(data[101,], pch = 15, col = 4, cex = 1.8)
		points(data[102,], pch = 16, col = 3, cex = 1.8)
		points(data[99,], pch = 18, col = 2, cex = 2.2)

		legend('bottomright', c("More obvious outlier","Less obvious outlier","Not obvious outlier"), pch = c(15,16,18), col=c(4,3,2), bty ="n", pt.cex = c(1.8,1.8,2.2))
		dev.off()

		sc1 = c(1, 0.77, 0.56)
		sc2 = c(1, 0.56, 0.77)
		sc3 = c(0.77, 1, 0.56)
		sc4 = c(0.77, 0.56, 1)
		sc5 = c(0.56, 1, 0.77)
		sc6 = c(0.56, 0.77, 1)
		
		file = "/home/henrique/Documents/journal/acmart-master/figs/sep_curves.pdf"
		pdf(file)
		#par(mgp=c(10,10,10))
		par(mar=c(4,4,4,4))
		x = 99
		i = seq(1,nrow(seps),x+1)
		j = c(101, 102, 99)
		plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
		points(seps[i[j[2]]:(i[j[2]]+x), 2], seps[i[j[2]]:(i[j[2]]+x), 3], type = "l", lty = 2)
		points(seps[i[j[3]]:(i[j[3]]+x), 2], seps[i[j[3]]:(i[j[3]]+x), 3], type = "l", lty = 3)
		legend('bottomright', c("More obvious outlier","Less obvious outlier","Not obvious outlier"), lty = c(1,2,3),bty ="n")

		dev.off()

		file = "/home/henrique/Documents/journal/acmart-master/figs/avg_sep_curves.pdf"
		pdf(file)
		#par(mgp=c(10,10,10))
		par(mar=c(4,4,4,4))
		xs = seps[i[j[2]]:(i[j[2]]+x), 2]
		ys1 = (seps[i[j[1]]:(i[j[1]]+x), 3]*1 + seps[i[j[2]]:(i[j[2]]+x), 3]*0.75 + seps[i[j[3]]:(i[j[3]]+x), 3]*0.51)/ (1+0.75+0.51)
		ys2 = (seps[i[j[1]]:(i[j[1]]+x), 3]*1 + seps[i[j[2]]:(i[j[2]]+x), 3]*0.51 + seps[i[j[3]]:(i[j[3]]+x), 3]*0.75)/ (1+0.75+0.51)
		ys3 = (seps[i[j[1]]:(i[j[1]]+x), 3]*0.75 + seps[i[j[2]]:(i[j[2]]+x), 3]*1 + seps[i[j[3]]:(i[j[3]]+x), 3]*0.51)/ (1+0.75+0.51)
		ys4 = (seps[i[j[1]]:(i[j[1]]+x), 3]*0.75 + seps[i[j[2]]:(i[j[2]]+x), 3]*0.51 + seps[i[j[3]]:(i[j[3]]+x), 3]*1)/ (1+0.75+0.51)
		ys5 = (seps[i[j[1]]:(i[j[1]]+x), 3]*0.51 + seps[i[j[2]]:(i[j[2]]+x), 3]*1 + seps[i[j[3]]:(i[j[3]]+x), 3]*0.75)/ (1+0.75+0.51)
		ys6 = (seps[i[j[1]]:(i[j[1]]+x), 3]*0.51 + seps[i[j[2]]:(i[j[2]]+x), 3]*0.75 + seps[i[j[3]]:(i[j[3]]+x), 3]*1)/ (1+0.75+0.51)

		plot(xs, ys1, type="l", lty=1, ylim=c(0,1), xlab=expression(gamma), ylab = expression(bar(p)))
		#points(xs, ys2, type="l", lty=2)
		#points(xs, ys3, type="l", lty=3)
		#points(xs, ys4, type="l", lty=4)
		#points(xs, ys5, type="l", lty=2)
		points(xs, ys6, type="l", lty = 2)
		legend('bottomright', c("Truly best solution","False best solution"), lty = c(1,2),bty ="n")

		dev.off()
}

subsvm <- function(data){
	library(e1071)

	data[,3] = 0
	data[101,3] = 1

	incX = (max(data[,1]) - min(data[,1]))*0.05
	incY = (max(data[,2]) - min(data[,2]))*0.05
	px1 <- seq(min(data[,1])-incX, max(data[,1])+incX, length = grid)
	px2 <- seq(min(data[,2])-incY, max(data[,2])+incY, length = grid)

	dat = data.frame(y = factor(data$V3), data[,1:2])

	xgrid = expand.grid(V1 = px1, V2 = px2)
	fit_svm = svm(factor(y) ~ ., data = dat, scale = T, cost = 999999999999, kernel ="polynomial", type="C-classification", coef0 = 25, degree = 2)
	func = predict(fit_svm, xgrid, decision.values = TRUE)
	func = attributes(func)$decision

	pdf("/home/henrique//poly1.pdf", width = 3, height = 3)
	par(xpd=T, mar=c(0,0,0,0))
	par(mgp=c(10,10,10))
	plot(dat[,2:3], cex = 0.5, xlim=c(min(px1), max(px1)), ylim=c(min(px2), max(px2)))
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], pch = 15, cex = 0.5, col = 2)
	contour(px1, px2, matrix(func, length(px1), length(px2)), level = 0, add = TRUE, drawlabels=F, lwd = 1.75)
	dev.off()

	dat = data.frame(y = factor(data[1:101,3]), data[1:101,1:2])

	xgrid = expand.grid(V1 = px1, V2 = px2)
	fit_svm = svm(factor(y) ~ ., data = dat, scale = T, cost = 999999999999, kernel ="polynomial", type="C-classification", coef0 = 25, degree = 2)
	func = predict(fit_svm, xgrid, decision.values = TRUE)
	func = attributes(func)$decision

	pdf("/home/henrique/poly2.pdf", width = 3, height = 3)
	par(xpd=T, mar=c(0,0,0,0))
	par(mgp=c(10,10,10))
	plot(dat[,2:3], cex = 0.5, xlim=c(min(px1), max(px1)), ylim=c(min(px2), max(px2)))
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], pch = 15, cex = 0.5, col = 2)
	contour(px1, px2, matrix(func, length(px1), length(px2)), level = 0, add = TRUE, drawlabels=F, lwd = 1.75)
	dev.off()

	dat = data.frame(y = factor(data[101:102,3]), data[101:102,1:2])

	xgrid = expand.grid(V1 = px1, V2 = px2)
	fit_svm = svm(factor(y) ~ ., data = dat, scale = T, cost = 999999999999, kernel ="polynomial", type="C-classification", coef0 = 25, degree = 2)
	func = predict(fit_svm, xgrid, decision.values = TRUE)
	func = attributes(func)$decision

	pdf("/home/henrique/poly3.pdf", width = 3, height = 3)
	par(xpd=T, mar=c(0,0,0,0))
	par(mgp=c(10,10,10))
	plot(dat[,2:3], cex = 0.5, xlim=c(min(px1), max(px1)), ylim=c(min(px2), max(px2)))
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], col = "white", cex = 0.5)
	points(dat[which(dat$y == 1),2:3], pch = 15, cex = 0.5, col = 2)
	contour(px1, px2, matrix(func, length(px1), length(px2)), level = 0, add = TRUE, drawlabels=F, lwd = 1.75)
	dev.off()

}

adaptive <- function(){
	seps = read.table("/home/henrique/ireos_extension/Paper/acmart-master/data/mahalanobis")
	trap = read.table("/home/henrique/ireos_extension/Paper/acmart-master/data/trap")
	trap = trap[which(trap[,2] == 100), 3:4]
	pdf("trap1.pdf")
	x = 99
	i = seq(1,nrow(seps),x+1)
	j = c(101, 102, 99)
	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8),1]
	b = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8),2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	dev.off()

	pdf("trap2.pdf")
	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8 | trap[,1] == 2 | trap[,1] == 6 ),1]
	b = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8 | trap[,1] == 2 | trap[,1] == 6 ),2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	points(x=c(4,4), y= c(0,b[3]), type="l", lty=3)
	dev.off()

	pdf("trap3.pdf")
	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8 | trap[,1] == 2 | trap[,1] == 6 | trap[,1] == 1 | trap[,1] == 3),1]
	b = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8 | trap[,1] == 2 | trap[,1] == 6 | trap[,1] == 1 | trap[,1] == 3),2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(4,4), y= c(0,b[3]), type="l", lty=3)
	points(x=c(2,2), y= c(0,b[4]), type="l", lty=3)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	dev.off()

	pdf("trap4.pdf")
	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8 | trap[,1] == 2 | trap[,1] == 6 | trap[,1] == 1 | trap[,1] == 3 | trap[,1] == 0.5 | trap[,1] == 1.5),1]
	b = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8 | trap[,1] == 2 | trap[,1] == 6 | trap[,1] == 1 | trap[,1] == 3 | trap[,1] == 0.5 | trap[,1] == 1.5),2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(4,4), y= c(0,b[3]), type="l", lty=3)
	points(x=c(2,2), y= c(0,b[4]), type="l", lty=3)
	points(x=c(1,1), y= c(0,b[5]), type="l", lty=3)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	dev.off()

	pdf("trap5.pdf")
	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8 | trap[,1] == 2 | trap[,1] == 6 | trap[,1] == 1 | trap[,1] == 3 | trap[,1] == 0.5 | trap[,1] == 1.5| trap[,1] == 0.75 | trap[,1] == 0.25),1]
	b = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8 | trap[,1] == 2 | trap[,1] == 6 | trap[,1] == 1 | trap[,1] == 3 | trap[,1] == 0.5 | trap[,1] == 1.5| trap[,1] == 0.75 | trap[,1] == 0.25),2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(4,4), y= c(0,b[3]), type="l", lty=3)
	points(x=c(2,2), y= c(0,b[4]), type="l", lty=3)
	points(x=c(1,1), y= c(0,b[5]), type="l", lty=3)
	points(x=c(0.5,0.5), y= c(0,b[6]), type="l", lty=3)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	dev.off()

	pdf("trap6.pdf")
	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[,1]
	b = trap[,2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(4,4), y= c(0,b[3]), type="l", lty=3)
	points(x=c(2,2), y= c(0,b[4]), type="l", lty=3)
	points(x=c(1,1), y= c(0,b[5]), type="l", lty=3)
	points(x=c(0.5,0.5), y= c(0,b[6]), type="l", lty=3)
	points(x=c(0.25,0.25), y= c(0,b[7]), type="l", lty=3)
	points(x=c(0.75,0.75), y= c(0,b[8]), type="l", lty=3)
	points(x=c(a[17],a[17]), y= c(0,b[17]), type="l", lty=3)
	points(x=c(a[12],a[12]), y= c(0,b[12]), type="l", lty=3)
	points(x=c(a[14],a[14]), y= c(0,b[14]), type="l", lty=3)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	dev.off()

	pdf("trap21.pdf")
	trap = read.table("/home/henrique/ireos_extension/Paper/acmart-master/data/trap")
	trap = trap[which(trap[,2] == 101), 3:4]
	x = 99
	i = seq(1,nrow(seps),x+1)
	j = c(101, 102, 99)
	plot(seps[i[j[2]]:(i[j[2]]+x), 2], seps[i[j[2]]:(i[j[2]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8),1]
	b = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8),2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	dev.off()

	pdf("trap22.pdf")
	plot(seps[i[j[2]]:(i[j[2]]+x), 2], seps[i[j[2]]:(i[j[2]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[which(trap[,1] == 0 | trap[,1] == 2 | trap[,1] == 4 | trap[,1] == 6 | trap[,1] == 8 ),1]
	b = trap[which(trap[,1] == 0 | trap[,1] == 2 | trap[,1] == 4 | trap[,1] == 6 | trap[,1] == 8 ),2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(4,4), y= c(0,b[3]), type="l", lty=3)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	dev.off()

	pdf("trap23.pdf")
	plot(seps[i[j[2]]:(i[j[2]]+x), 2], seps[i[j[2]]:(i[j[2]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[which(trap[,1] == 0 | trap[,1] == 1 | trap[,1] == 2 | trap[,1] == 3 | trap[,1] == 4 | trap[,1] == 5 | trap[,1] == 6 | trap[,1] == 7 | trap[,1] == 8),1]
	b = trap[which(trap[,1] == 0 | trap[,1] == 1 | trap[,1] == 2 | trap[,1] == 3 | trap[,1] == 4 | trap[,1] == 5 | trap[,1] == 6 | trap[,1] == 7 | trap[,1] == 8),2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(2,2), y= c(0,b[4]), type="l", lty=3)
	points(x=c(4,4), y= c(0,b[3]), type="l", lty=3)
	points(x=c(6,6), y= c(0,b[5]), type="l", lty=3)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	dev.off()

	pdf("trap24.pdf")
	plot(seps[i[j[2]]:(i[j[2]]+x), 2], seps[i[j[2]]:(i[j[2]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[,1]
	b = trap[,2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(1,1), y= c(0,b[6]), type="l", lty=3)
	points(x=c(2,2), y= c(0,b[4]), type="l", lty=3)
	points(x=c(3,3), y= c(0,b[9]), type="l", lty=3)
	points(x=c(4,4), y= c(0,b[3]), type="l", lty=3)
	points(x=c(6,6), y= c(0,b[5]), type="l", lty=3)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	dev.off()

	pdf("trap31.pdf")
	trap = read.table("/home/henrique/ireos_extension/Paper/acmart-master/data/trap")
	trap = trap[which(trap[,2] == 98), 3:4]
	x = 99
	i = seq(1,nrow(seps),x+1)
	j = c(101, 102, 99)
	plot(seps[i[j[3]]:(i[j[3]]+x), 2], seps[i[j[3]]:(i[j[3]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8),1]
	b = trap[which(trap[,1] == 0 | trap[,1] == 4 | trap[,1] == 8),2]
	points(x = a[order(a)], y = b[order(a)], type = "l", lty = 2)
	points(x=a, y = b)
	points(x=c(0,0), y= c(0,b[1]), type="l", lty=3)
	points(x=c(8,8), y= c(0,b[2]), type="l", lty=3)
	dev.off()


	simp = read.table("/home/henrique/ireos_extension/Paper/acmart-master/data/simp")
	simp = simp[which(simp[,2] == 100), 3:4]
	x = 99
	i = seq(1,nrow(seps),x+1)
	j = c(101, 102, 99)
	pdf("simp.pdf")
	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = simp[which(simp[,1] == 0 | simp[,1] == 2 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 0 | simp[,1] == 2 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(0,4, 0.2)
	points(x0, 0.00982303756145622 + 0.742632519290521*x0 - 0.123772069670227*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 6 | simp[,1] == 8 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 6 | simp[,1] == 8 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(4,8, 0.2)
	points(x0, 0.99999999999949 + 1.48769885299771e-13*x0 - 1.06303854607859e-14*x0^2, type="l", lty=2)

	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = simp[which(simp[,1] == 0 | simp[,1] == 2 | simp[,1] == 1),1]
	b = simp[which(simp[,1] == 0 | simp[,1] == 2 | simp[,1] == 1),2]
	lagrange.poly(a, b) 
	x0 = seq(0,2, 0.2)
	points(x0, 0.00982303756145622 + 1.48367178018795*x0 - 0.49429170011894*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 2 | simp[,1] == 3 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 2 | simp[,1] == 3 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(2,4, 0.2)
	points(x0, 0.999998785604434 + 7.08258288462105e-7*x0 - 1.01164854493518e-7*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 5 | simp[,1] == 6 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 5 | simp[,1] == 6 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(4,6, 0.2)
	points(x0, 0.999999999998725 + 4.68070027181966e-13*x0 - 4.25215418431435e-14*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 8 | simp[,1] == 6 | simp[,1] == 7),1]
	b = simp[which(simp[,1] == 8 | simp[,1] == 6 | simp[,1] == 7),2]
	lagrange.poly(a, b) 
	x0 = seq(6,8, 0.2)
	points(x0, x0^0, type="l", lty=2)

	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = simp[which(simp[,1] == 0 | simp[,1] == 0.5 | simp[,1] == 1),1]
	b = simp[which(simp[,1] == 0 | simp[,1] == 0.5 | simp[,1] == 1),2]
	lagrange.poly(a, b) 
	x0 = seq(0,1, 0.2)
	points(x0, 0.00982303756145622 + 2.57861635702514*x0 - 1.58923627695614*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 1 | simp[,1] == 1.5 | simp[,1] == 2),1]
	b = simp[which(simp[,1] == 1 | simp[,1] == 1.5 | simp[,1] == 2),2]
	lagrange.poly(a, b) 
	x0 = seq(1,2, 0.2)
	points(x0, 0.995306972137055 + 0.00544587832454901*x0 - 0.00154973283114046*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 2 | simp[,1] == 2.5 | simp[,1] == 3),1]
	b = simp[which(simp[,1] == 2 | simp[,1] == 2.5 | simp[,1] == 3),2]
	lagrange.poly(a, b) 
	x0 = seq(2,3, 0.2)
	points(x0, 0.999997064158965 + 2.14279617871682e-6*x0 - 3.88072432411235e-7*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 3 | simp[,1] == 3.5 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 3 | simp[,1] == 3.5 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(3,4, 0.2)
	points(x0, 0.999999997211603 + 1.48743950489916e-9*x0 - 1.97590388495428e-10*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 5 | simp[,1] == 6 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 5 | simp[,1] == 6 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(4,6, 0.2)
	points(x0, 0.999999999998725 + 4.68070027181966e-13*x0 - 4.25215418431435e-14*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 8 | simp[,1] == 6 | simp[,1] == 7),1]
	b = simp[which(simp[,1] == 8 | simp[,1] == 6 | simp[,1] == 7),2]
	lagrange.poly(a, b) 
	x0 = seq(6,8, 0.2)
	points(x0, x0^0, type="l", lty=2)

	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = simp[which(simp[,1] == 0 | simp[,1] == 0.5 | simp[,1] == 0.25),1]
	b = simp[which(simp[,1] == 0 | simp[,1] == 0.5 | simp[,1] == 0.25),2]
	lagrange.poly(a, b) 
	x0 = seq(0,0.5, 0.2)
	points(x0, 0.00982303756145622 + 0.584164903014115*x0 + 2.39966663106592*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 0.5 | simp[,1] == 0.75 | simp[,1] == 1),1]
	b = simp[which(simp[,1] == 0.5 | simp[,1] == 0.75 | simp[,1] == 1),2]
	lagrange.poly(a, b) 
	x0 = seq(0.5,1, 0.2)
	points(x0, 0.473504749585723 + 1.18757122095234*x0 - 0.6618728529076*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 1 | simp[,1] == 1.25 | simp[,1] == 1.5),1]
	b = simp[which(simp[,1] == 1 | simp[,1] == 1.25 | simp[,1] == 1.5),2]
	lagrange.poly(a, b) 
	x0 = seq(1,1.5, 0.2)
	points(x0, 0.998262985530534 + 0.00596348879318676*x0 - 0.00502335669325671*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 1.5 | simp[,1] == 1.75 | simp[,1] == 2),1]
	b = simp[which(simp[,1] == 1.5 | simp[,1] == 1.75 | simp[,1] == 2),2]
	lagrange.poly(a, b) 
	x0 = seq(1.5,2, 0.2)
	points(x0, 0.999755056864451 + 0.000256446142586242*x0 - 6.70379220082395e-5*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 2 | simp[,1] == 2.5 | simp[,1] == 3),1]
	b = simp[which(simp[,1] == 2 | simp[,1] == 2.5 | simp[,1] == 3),2]
	lagrange.poly(a, b) 
	x0 = seq(2,3, 0.2)
	points(x0, 0.999997064158965 + 2.14279617871682e-6*x0 - 3.88072432411235e-7*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 3 | simp[,1] == 3.5 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 3 | simp[,1] == 3.5 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(3,4, 0.2)
	points(x0, 0.999999997211603 + 1.48743950489916e-9*x0 - 1.97590388495428e-10*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 5 | simp[,1] == 6 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 5 | simp[,1] == 6 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(4,6, 0.2)
	points(x0, 0.999999999998725 + 4.68070027181966e-13*x0 - 4.25215418431435e-14*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 8 | simp[,1] == 6 | simp[,1] == 7),1]
	b = simp[which(simp[,1] == 8 | simp[,1] == 6 | simp[,1] == 7),2]
	lagrange.poly(a, b) 
	x0 = seq(6,8, 0.2)
	points(x0, x0^0, type="l", lty=2)

	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = simp[which(simp[,1] == 0 | simp[,1] == 0.125 | simp[,1] == 0.25),1]
	b = simp[which(simp[,1] == 0 | simp[,1] == 0.125 | simp[,1] == 0.25),2]
	lagrange.poly(a, b) 
	x0 = seq(0,0.25, 0.2)
	points(x0, 0.00982303756145622 - 0.234732317916451*x0 + 5.67525551478819*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 0.25 | simp[,1] == 0.375 | simp[,1] == 0.5),1]
	b = simp[which(simp[,1] == 0.25 | simp[,1] == 0.375 | simp[,1] == 0.5),2]
	lagrange.poly(a, b) 
	x0 = seq(0.25,0.5, 0.2)
	points(x0, -0.930090529219644 + 6.22364630370072*x0 - 5.11964190318288*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 0.5 | simp[,1] == 0.625 | simp[,1] == 0.75),1]
	b = simp[which(simp[,1] == 0.5 | simp[,1] == 0.625 | simp[,1] == 0.75),2]
	lagrange.poly(a, b) 
	x0 = seq(0.5,0.75, 0.2)
	points(x0, 0.114374496091706 + 2.3846720659324*x0 - 1.61955352889164*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 0.75 | simp[,1] == 0.875 | simp[,1] == 1),1]
	b = simp[which(simp[,1] == 0.75 | simp[,1] == 0.875 | simp[,1] == 1),2]
	lagrange.poly(a, b) 
	x0 = seq(0.75,1, 0.2)
	points(x0, 0.875434220108602 + 0.249735789732284*x0 - 0.125966892210432*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 1 | simp[,1] == 1.25 | simp[,1] == 1.5),1]
	b = simp[which(simp[,1] == 1 | simp[,1] == 1.25 | simp[,1] == 1.5),2]
	lagrange.poly(a, b) 
	x0 = seq(1,1.5, 0.2)
	points(x0, 0.998262985530534 + 0.00596348879318676*x0 - 0.00502335669325671*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 1.5 | simp[,1] == 1.75 | simp[,1] == 2),1]
	b = simp[which(simp[,1] == 1.5 | simp[,1] == 1.75 | simp[,1] == 2),2]
	lagrange.poly(a, b) 
	x0 = seq(1.5,2, 0.2)
	points(x0, 0.999755056864451 + 0.000256446142586242*x0 - 6.70379220082395e-5*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 2 | simp[,1] == 2.5 | simp[,1] == 3),1]
	b = simp[which(simp[,1] == 2 | simp[,1] == 2.5 | simp[,1] == 3),2]
	lagrange.poly(a, b) 
	x0 = seq(2,3, 0.2)
	points(x0, 0.999997064158965 + 2.14279617871682e-6*x0 - 3.88072432411235e-7*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 3 | simp[,1] == 3.5 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 3 | simp[,1] == 3.5 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(3,4, 0.2)
	points(x0, 0.999999997211603 + 1.48743950489916e-9*x0 - 1.97590388495428e-10*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 5 | simp[,1] == 6 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 5 | simp[,1] == 6 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(4,6, 0.2)
	points(x0, 0.999999999998725 + 4.68070027181966e-13*x0 - 4.25215418431435e-14*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 8 | simp[,1] == 6 | simp[,1] == 7),1]
	b = simp[which(simp[,1] == 8 | simp[,1] == 6 | simp[,1] == 7),2]
	lagrange.poly(a, b) 
	x0 = seq(6,8, 0.2)
	points(x0, x0^0, type="l", lty=2)

	plot(seps[i[j[1]]:(i[j[1]]+x), 2], seps[i[j[1]]:(i[j[1]]+x), 3], type = "l", lty = 1, ylim=c(0,1), xlab=expression(gamma), ylab = "p")
	a = simp[which(simp[,1] == 0 | simp[,1] == 0.125 | simp[,1] == 0.0625),1]
	b = simp[which(simp[,1] == 0 | simp[,1] == 0.125 | simp[,1] == 0.0625),2]
	lagrange.poly(a, b) 
	x0 = seq(0,0.125, 0.2)
	points(x0, 0.00982303756145622 + 0.0832353222716236*x0 + 3.13151439328359*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 0.125 | simp[,1] == 0.1875 | simp[,1] == 0.25),1]
	b = simp[which(simp[,1] == 0.125 | simp[,1] == 0.1875 | simp[,1] == 0.25),2]
	lagrange.poly(a, b) 
	x0 = seq(0.125,0.25, 0.2)
	points(x0, 0.0785197800727102 - 1.0590932280515*x0 + 7.87355127514831*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 0.25 | simp[,1] == 0.3125 | simp[,1] == 0.375),1]
	b = simp[which(simp[,1] == 0.25 | simp[,1] == 0.3125 | simp[,1] == 0.375),2]
	lagrange.poly(a, b) 
	x0 = seq(0.25,0.375, 0.2)
	points(x0, -0.571812912466725 + 3.83512885868125*x0 - 1.29801399115175*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 0.375 | simp[,1] == 0.4375 | simp[,1] == 0.5),1]
	b = simp[which(simp[,1] == 0.375 | simp[,1] == 0.4375 | simp[,1] == 0.5),2]
	lagrange.poly(a, b) 
	x0 = seq(0.375,0.5, 0.2)
	points(x0, -1.22397997301295 + 7.59513037473616*x0 - 6.68705227008053*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 0.5 | simp[,1] == 0.625 | simp[,1] == 0.75),1]
	b = simp[which(simp[,1] == 0.5 | simp[,1] == 0.625 | simp[,1] == 0.75),2]
	lagrange.poly(a, b) 
	x0 = seq(0.5,0.75, 0.2)
	points(x0, 0.114374496091706 + 2.3846720659324*x0 - 1.61955352889164*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 0.75 | simp[,1] == 0.875 | simp[,1] == 1),1]
	b = simp[which(simp[,1] == 0.75 | simp[,1] == 0.875 | simp[,1] == 1),2]
	lagrange.poly(a, b) 
	x0 = seq(0.75,1, 0.2)
	points(x0, 0.875434220108602 + 0.249735789732284*x0 - 0.125966892210432*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 1 | simp[,1] == 1.25 | simp[,1] == 1.5),1]
	b = simp[which(simp[,1] == 1 | simp[,1] == 1.25 | simp[,1] == 1.5),2]
	lagrange.poly(a, b) 
	x0 = seq(1,1.5, 0.2)
	points(x0, 0.998262985530534 + 0.00596348879318676*x0 - 0.00502335669325671*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 1.5 | simp[,1] == 1.75 | simp[,1] == 2),1]
	b = simp[which(simp[,1] == 1.5 | simp[,1] == 1.75 | simp[,1] == 2),2]
	lagrange.poly(a, b) 
	x0 = seq(1.5,2, 0.2)
	points(x0, 0.999755056864451 + 0.000256446142586242*x0 - 6.70379220082395e-5*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 2 | simp[,1] == 2.5 | simp[,1] == 3),1]
	b = simp[which(simp[,1] == 2 | simp[,1] == 2.5 | simp[,1] == 3),2]
	lagrange.poly(a, b) 
	x0 = seq(2,3, 0.2)
	points(x0, 0.999997064158965 + 2.14279617871682e-6*x0 - 3.88072432411235e-7*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 3 | simp[,1] == 3.5 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 3 | simp[,1] == 3.5 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(3,4, 0.2)
	points(x0, 0.999999997211603 + 1.48743950489916e-9*x0 - 1.97590388495428e-10*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 5 | simp[,1] == 6 | simp[,1] == 4),1]
	b = simp[which(simp[,1] == 5 | simp[,1] == 6 | simp[,1] == 4),2]
	lagrange.poly(a, b) 
	x0 = seq(4,6, 0.2)
	points(x0, 0.999999999998725 + 4.68070027181966e-13*x0 - 4.25215418431435e-14*x0^2, type="l", lty=2)
	a = simp[which(simp[,1] == 8 | simp[,1] == 6 | simp[,1] == 7),1]
	b = simp[which(simp[,1] == 8 | simp[,1] == 6 | simp[,1] == 7),2]
	lagrange.poly(a, b) 
	x0 = seq(6,8, 0.2)
	points(x0, x0^0, type="l", lty=2)
	dev.off()
}
#0 2 4 6 8
#0 1 2 3 4 5 6 7 8
#0 0.5 1 1.5 2 2.5 3 3.5 4
#0 0.25 0.5 0.75 1 1.25 1.5 1.75 2
#0 0.1250 0.25 0.3750 0.5 0.6250 0.75 0.875 1
#0 0.0625 0.1250 0.1875 0.25 0.3125 0.3750 0.4375 0.5


lagrange.poly <- function(x, y) {
	library(rSymPy)
	l <- list() # List to store Lagrangian polynomials L_{1,2,3,4}
	k <- 1
	for (i in x) {
		# Set the numerator and denominator of the Lagrangian polynomials to 1 and build them up
		num <- 1
		denom <- 1
		# Remove the current x value from the iterated list
		p <- x[! x %in% i]
		# For the remaining points, construct the Lagrangian polynomial by successively 
		# appending each x value
		for (j in p) {
			num <- paste(num, "*", "(", 'x', " - ", as.character(j), ")", sep = "", collapse = "")
			denom <- paste(denom, "*", "(", as.character(i)," - ", as.character(j), ")", sep = "", collapse = "")
		}
		# Set each Lagrangian polynomial in rSymPy to simplify later.
		l[k] <- paste("(", num, ")", "/", "(", denom, ")", sep = "", collapse = "")
		k <- k + 1
	}
	# Similar to before, we construct the final Lagrangian polynomial by successively building 
	# up the equation by iterating through the polynomials L_{1,2,3,4} and the y values 
	# corresponding to the x values.
	eq <- 0
	for (i in 1:length(y)) {
		eq <- paste(eq, '+', as.character(y[i]), "*", l[[i]], sep = "", collapse = "")
	}
	# Define x variable for rSymPy to simplify
	x <- Var('x')
	# Simplify the result with rSymPy and return the polynomial
	return(sympy(paste("simplify(", eq, ")")))
}