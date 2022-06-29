//dllmain.cpp : Определяет точку входа для приложения DLL.
#include "pch.h"

#include "JNI.h"
#include <filesystem>
#include <iostream>
#include <direct.h>
#include <string>
#include <vector>
#include <sstream>
#include <fstream>
#include <opencv2/opencv.hpp>


BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
                     )
{
    switch (ul_reason_for_call)
    {
    case DLL_PROCESS_ATTACH:
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE;
}


using namespace cv;
using namespace std;

String findDirName(String filePath) {
	stringstream fullFilePath(filePath);
	string segment;
	vector<string> segList;

	while (getline(fullFilePath, segment, '/'))
		segList.push_back(segment);

	return filePath.substr(0, filePath.length() - segList[segList.size() - 1].length());
}
String extensionOf(String filePath) {
	stringstream fullFilePath(filePath);
	string segment;
	vector<string> segList;

	while (getline(fullFilePath, segment, '.'))
		segList.push_back(segment);

	return filePath.substr(filePath.length() - segList[segList.size() - 1].length() - 1);
}
struct DoublePoint {
	double x;
	double y;
	DoublePoint() {
		x = 0;
		y = 0;
	}
	DoublePoint(Point p) {
		x = p.x;
		y = p.y;
	}
	DoublePoint(double x, double y) {
		this->x = x;
		this->y = y;
	}
};
struct TABLE_FRAME {
	const DoublePoint LeftUpFraction = DoublePoint(1570.0 / 2335.0, 1380.0 / 1650.0);
};
struct LEFT_SHIFT_FRAME {
	const DoublePoint LeftUpFraction = DoublePoint(100.0 / 2340.0, 1);
};
class Line
{
public:
	Point p1;
	Point p2;
	Line(Point p1, Point p2) {
		this->p1 = p1;
		this->p2 = p2;
	}
	Line(DoublePoint& p1, DoublePoint& p2) {
		this->p1.x = p1.x;
		this->p1.y = p1.y;
		this->p2.x = p2.x;
		this->p2.y = p2.y;
	}
	bool match(Point p) {
		return (p1.y - p2.y) * p.x + (p2.x - p1.x) * p.y + p1.x * p2.y - p2.x * p1.y < 0.000001;
	}
};
class ImageProcessor {
private:
	int pageNumber = 0;
	int kernelSize;
	int minWallLen;
	int maxWallThickness;
	String filePath;
	Mat image;
	Mat noEnvironmentImage;
	Mat noWallsImage;
	vector<vector<Point>> outsideContour;
	vector<Line> innerContour;
public:
	ImageProcessor(String filePath, int kernelSize = 8, int minWallLen = -1, int contourExpansion = 2, int difference = 1) {
		if (minWallLen == -1)
			minWallLen = kernelSize * 4;

		this->filePath = filePath;
		this->kernelSize = kernelSize;
		this->minWallLen = minWallLen;
		this->maxWallThickness = kernelSize * 2;

		image = imread(filePath, IMREAD_GRAYSCALE);

		Mat noTableImage = fillTable(image);
		noEnvironmentImage = environmentalFilter(noTableImage, difference);
		
		noWallsImage = wallFilter(noEnvironmentImage);
		
		image.copyTo(noEnvironmentImage, noWallsImage);
		fillTheWalls(noEnvironmentImage);
		
		cropImageByContour();
		increaseContour(noWallsImage, contourExpansion);
		
		outsideContour = findOutsideContour();
		innerContour = findInnerContour();
	}
	ImageProcessor(String filePath, String noWallsName, int kernelSize = 8, int minWallLen = -1, int contourExpansion = 2) {
		if (minWallLen == -1)
			minWallLen = kernelSize * 4;

		this->filePath = filePath;
		this->kernelSize = kernelSize;
		this->minWallLen = minWallLen;
		this->maxWallThickness = kernelSize * 2;

		image = imread(filePath, IMREAD_GRAYSCALE);
		noEnvironmentImage = imread(noWallsName, IMREAD_GRAYSCALE);

		noWallsImage = wallFilter(noEnvironmentImage);

		image.copyTo(noEnvironmentImage, noWallsImage);
		fillTheWalls(noEnvironmentImage);
		noWallsImage = wallFilter(noEnvironmentImage);
		increaseContour(noWallsImage, contourExpansion);

		cropImageByContour();
		
		outsideContour = findOutsideContour();
		innerContour = findInnerContour();
	}
	Mat getNoEnvironmentImage() {
		return noEnvironmentImage;
	}
	Mat getNoWallsImage() {
		return noWallsImage;
	}
	Mat getImage() {
		return image;
	};
	vector<vector<Point>> getOutsideContour() {
		return outsideContour;
	}
	vector<Line> getInnerContour() {
		return innerContour;
	}
	Mat outsideContourCoverage() {
		vector<Line> lines = convertToLines(outsideContour);
		return contourToMatrix(lines, image);
	}
	Mat innerContourCoverage() {
		return contourToMatrix(innerContour, image);
	}
	Mat contourCoverage() {
		Mat inner = contourToMatrix(innerContour, image);
		return contourToMatrix(outsideContour, inner);
	}
	void writeImageToFile(Mat& inputMatrix, String fileName = "") {
		if (fileName == "")
			imwrite(makeFileName(filePath), inputMatrix);
		else
			imwrite(fileName, inputMatrix);
	};
	void writeInnerContourToFile(String dirName) {
		ofstream File(dirName + "inner.txt");
		for (int i = 0; i < innerContour.size(); i++) {
			File << innerContour[i].p1.x << " " << innerContour[i].p1.y << " ";
			File << innerContour[i].p2.x << " " << innerContour[i].p2.y << endl;
		}
		File.close();
	}
	void writeOutsideContourToFile(String dirName) {
		ofstream File(dirName + "outside.txt");

		for (int i = 0; i < outsideContour.size(); i++) {
			for (int j = 0; j < outsideContour[i].size(); j++)
				File << outsideContour[i][j].x << " " << outsideContour[i][j].y << " ";
			File << outsideContour[i][0].x << " " << outsideContour[i][0].y << endl;
		}
		File.close();
	}
private:
	vector<vector<Point>> findOutsideContour() {
		vector<vector<Point>> contours;
		findContours(noWallsImage, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
		return performOutsideContour(contours);
	}
	vector<Line> findInnerContour() {
		Mat inverse;
		bitwise_not(noEnvironmentImage, inverse, noWallsImage);
		vector<Line> lines;
		detectHorizontal(inverse, lines);
		detectVertical(inverse, lines);

		return lines;
	}
	vector<vector<Point>> performOutsideContour(vector<vector<Point>>& contours) {
		double error = kernelSize / 2;
		vector<vector<Point>> output;

		for (int i = 0; i < contours.size(); i++) {
			Point prev;
			vector<Point> new_cnt;
			vector<Point> close_buf;
			close_buf.push_back(contours[i][0]);
			for (int j = 1; j < contours[i].size(); j++) {
				if (dist(contours[i][j], close_buf[close_buf.size() - 1]) > error) {
					new_cnt.push_back(averagePoint(close_buf));
					close_buf.clear();
				}
				close_buf.push_back(contours[i][j]);
			}
			new_cnt.push_back(averagePoint(close_buf));
			if (new_cnt.size() > 0)
				output.push_back(new_cnt);
		}

		for (int i = 0; i < output.size(); i++)
			output[i].push_back(output[i][0]);

		return output;
	}
	Point averagePoint(vector<Point>& points) {
		int x = 0, y = 0;
		for (int i = 0; i < points.size(); i++){
			x += points[i].x;
			y += points[i].y;
		}
		x /= points.size();
		y /= points.size();
		return Point(x, y);
	}
	Mat fillTable(Mat& inputImage) {
		TABLE_FRAME frame;
		Mat noTableImage;
		inputImage.copyTo(noTableImage);
		int startX = noTableImage.cols * frame.LeftUpFraction.x;
		int startY = noTableImage.rows * frame.LeftUpFraction.y;
		fillArea(noTableImage, startX, startY, noTableImage.cols, noTableImage.rows);
		return noTableImage;
	}
	void fillArea(Mat& inputImage, int startX, int startY, int endX, int endY) {
		for (int x = startX; x < endX; x++)
			for (int y = startY; y < endY; y++)
				inputImage.at<uchar>(y, x) = 0;
	}
	void increaseContour(Mat& noWallsImg, int counter) {
		int color_error = 10;

		for (int m = 0; m < counter; m++) {
			for (int x = 0; x < noWallsImage.cols; x++) {
				int localMaxY = -1;
				for (int y = 0; y < noWallsImage.rows; y++)
					if (255 - noWallsImg.at<uchar>(y, x) < color_error) {
						if (localMaxY < y) localMaxY = y;
					}
					else if (localMaxY != -1) {
						noWallsImg.at<uchar>(localMaxY, x) = 0;
						localMaxY = 0;
					}

			}
			for (int y = 0; y < noWallsImage.rows; y++) {
				int localMaxX = -1;
				for (int x = 0; x < noWallsImage.cols; x++)
					if (255 - noWallsImg.at<uchar>(y, x) < color_error) {
						if (localMaxX < x) localMaxX = x;
					}
					else if (localMaxX != -1) {
						noWallsImg.at<uchar>(y, localMaxX) = 0;
						localMaxX = 0;
					}
			}
		}
	}
	vector<vector<Point>> contourVector(Mat& input) {
		vector<vector<Point> > contours;
		vector<Vec4i> hierarchy;
		findContours(input, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
		return contours;
	};
	Mat environmentalFilter(Mat& input, int difference) {
		Mat buf, output;
		Mat kernelErode = getStructuringElement(MORPH_RECT, Size(kernelSize, kernelSize)) * 255;
		Mat kernelDelate = getStructuringElement(MORPH_RECT, Size(kernelSize + difference, kernelSize + difference)) * 255;

		//remove lines with a thickness of 1
		erode(input, buf, kernelErode);
		dilate(buf, output, kernelDelate);

		//fill the environment 
		floodFill(output, Point(1, 1), Scalar(0, 0, 0));
		floodFill(output, Point(kernelSize * 2, kernelSize * 2), Scalar(0, 0, 0));
		
		//clear the image
		Mat kernelOpening = getStructuringElement(MORPH_RECT, Size(kernelSize, kernelSize)) * 255;
		erode(output, buf, kernelOpening);
		dilate(buf, output, kernelOpening);
		
		return output;
	}
	void fillTheWalls(Mat& input) {
		Mat buf;
		Mat kernel = getStructuringElement(MORPH_RECT, Size(kernelSize, kernelSize)) * 255;

		erode(input, buf, kernel);
		dilate(buf, input, kernel);
	}
	Mat wallFilter(Mat& input, int kernelMultiplier = 5) {
		int size = kernelSize * kernelMultiplier;
		Mat buf, output;
		Mat kernelClosing = getStructuringElement(MORPH_RECT, Size(size, size)) * 255;

		dilate(input, buf, kernelClosing);
		erode(buf, output, kernelClosing);

		return output;
	}
	void cropImageByContour() {

		int frameThickness = min(image.cols / 40, image.rows / 40);

		vector<vector<Point>> contour = contourVector(noEnvironmentImage);
	
		int startX = contour[0][0].x, startY = contour[0][0].y;
		int endX = contour[0][0].x, endY = contour[0][0].y;

		for (int i = 0; i < contour.size(); i++) {
			for (int j = 0; j < contour[i].size(); j++) {
				if (contour[i][j].x < startX) startX = contour[i][j].x;
				if (contour[i][j].y < startY) startY = contour[i][j].y;
				if (contour[i][j].x > endX) endX = contour[i][j].x;
				if (contour[i][j].y > endY) endY = contour[i][j].y;
			}
		}

		int width = endX - startX + 2 * frameThickness;
		int height = endY - startY + 2 * frameThickness;

		startX = startX - frameThickness;
		startY = startY - frameThickness;

		if (startX < 0) startX = 0;
		if (startY < 0) startY = 0;

		if (width + startX > image.cols) width = image.cols - startX;
		if (height + startY > image.rows) height = image.rows - startY;


		image = Mat(image, Rect(startX, startY, width, height));
		noEnvironmentImage = Mat(noEnvironmentImage, Rect(startX, startY, width, height));
		noWallsImage = Mat(noWallsImage, Rect(startX, startY, width, height));
	}
	String makeFileName(String fileName) {
		String fileFormat = extensionOf(fileName);
		pageNumber++;
		String newFileName = fileName.substr(0, fileName.length() - fileFormat.length()) + to_string(pageNumber) + fileFormat;
		return newFileName;
	}
	vector<Line> convertToLines(vector<vector<Point>>& contours) {
		vector<Line> lines;
		for (int i = 0; i < contours.size(); i++) {
			for (int j = 0; j < contours[i].size() - 1; j++)
				lines.push_back(Line(contours[i][j], contours[i][j + 1]));
			lines.push_back(Line(contours[i][contours[i].size() - 1], contours[i][0]));
		}
		return lines;
	}
	vector<Line> convertToLines(vector<Point>& contours) {
		vector<Line> lines;
		for (int i = 0; i < contours.size() - 1; i++)
			lines.push_back(Line(contours[i], contours[i + 1]));
		lines.push_back(Line(contours[contours.size() - 1], contours[0]));
		return lines;
	}
	double dist(Point p1, Point p2) {
		double A = p2.x - p1.x;
		double B = p2.y - p1.y;
		A *= A;
		B *= B;
		return sqrt(A + B);
	}
	Mat contourToMatrix(vector<Line>& lines, Size imageSize, int imageType) {
		Mat drawing = Mat::zeros(imageSize, imageType);
		Scalar color = Scalar(255, 255, 255);
		int thickness = 10;
		for (int i = 0; i < lines.size(); i++)
			line(drawing, lines[i].p1, lines[i].p2, color, thickness);

		return drawing;
	}
	Mat contourToMatrix(vector<Line>& lines, Mat& paintImage) {

		Mat drawing = Mat(paintImage.rows, paintImage.cols, paintImage.type());
		paintImage.convertTo(drawing, drawing.type());
		Scalar colorLine = Scalar(0, 0, 0);
		Scalar colorCircle = Scalar(255, 255, 255);
		int thickness = 10;

		for (int i = 0; i < lines.size(); i++) {
			line(drawing, lines[i].p1, lines[i].p2, colorLine, thickness);
			circle(drawing, lines[i].p1, thickness, colorCircle);
			circle(drawing, lines[i].p2, thickness, colorCircle);
		}


		return drawing;
	}
	Mat contourToMatrix(vector<vector<Point>>& contours, Mat& paintImage) {
		Mat drawing;
		image.copyTo(drawing);
		Scalar colorCircle = Scalar(255, 255, 255);
		int thickness = 10;
		for (int i = 0; i < contours.size(); i++) {
			drawContours(drawing, contours, i, Scalar(0, 0, 0), thickness);
			for (int j = 0; j < contours[i].size(); j++) {
				circle(drawing, contours[i][j], thickness, colorCircle);
			}
		}
		return drawing;
	}
	void detectHorizontal(Mat& inverse, vector<Line>& lines_output) {

		Mat horizontal_kernel = getStructuringElement(MORPH_RECT, Size(minWallLen, 1));
		Mat	detect_horizontal;
		morphologyEx(inverse, detect_horizontal, MORPH_OPEN, horizontal_kernel);

		vector<vector<Point>> contours;
		findContours(detect_horizontal, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

		for (int i = 0; i < contours.size(); i++) {
			vector<Line> lines = convertToLines(contours[i]);
			vector<Line> approxLines = approximateHorizontalLines(lines, maxWallThickness);
			for (int j = 0; j < approxLines.size(); j++)
				lines_output.push_back(approxLines[j]);
		}
	}
	void detectVertical(Mat& inverse, vector<Line>& lines_output) {

		Mat vertical_kernel = getStructuringElement(MORPH_RECT, Size(1, minWallLen));
		Mat	detect_vertical;
		morphologyEx(inverse, detect_vertical, MORPH_OPEN, vertical_kernel);

		vector<vector<Point>> contours;
		findContours(detect_vertical, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

		for (int i = 0; i < contours.size(); i++) {
			vector<Line> lines = convertToLines(contours[i]);
			vector<Line> approxLines = approximateVerticalLines(lines, maxWallThickness);
			for (int j = 0; j < approxLines.size(); j++)
				lines_output.push_back(approxLines[j]);
		}
	}
	vector<Line> approximateHorizontalLines(vector<Line>& contour, int max_dif) {
		int* idxArr = new int[contour.size()];
		for (int i = 0; i < contour.size(); i++)
			idxArr[i] = -1;

		for (int i = 0; i < contour.size(); i++) {
			if (!isHorizontal(contour[i])) continue;
			for (int j = i + 1; j < contour.size(); j++)
				if (isHorizontal(contour[j]) && horizontalDistance(contour[i], contour[j]) < max_dif) {
					if (idxArr[i] == -1) 
						idxArr[i] = i;
					idxArr[j] = idxArr[i];
				}
		}

		vector<Line> out;
		for (int i = 0; i < contour.size(); i++) {
			if (idxArr[i] == -1) continue;
			out.push_back(processHorizontal(idxArr, idxArr[i], contour));
		}

		return out;
	}
	vector<Line> approximateVerticalLines(vector<Line>& contour, int max_dif) {
		int* idxArr = new int[contour.size()];
		for (int i = 0; i < contour.size(); i++)
			idxArr[i] = -1;

		for (int i = 0; i < contour.size(); i++) {
			if (!isVertical(contour[i])) continue;
			for (int j = i + 1; j < contour.size(); j++)
				if (isVertical(contour[j]) && verticalDistance(contour[i], contour[j]) < max_dif) {
					if (idxArr[i] == -1) 
						idxArr[i] = i;
					idxArr[j] = idxArr[i];
				}
		}

		vector<Line> out;
		for (int i = 0; i < contour.size(); i++) {
			if (idxArr[i] == -1) continue;
			out.push_back(processVertical(idxArr, idxArr[i], contour));
		}

		return out;
	}
	Line processHorizontal(int* idxArr, int current_idx, vector<Line>& contour) {
		int minX = image.cols, maxX = -1, pos_y = 0, counter = 0;

		for (int i = 0; i < contour.size(); i++)
			if (idxArr[i] == current_idx) {
				if (minX > contour[i].p1.x) minX = contour[i].p1.x;
				if (minX > contour[i].p2.x) minX = contour[i].p2.x;
				if (maxX < contour[i].p1.x) maxX = contour[i].p1.x;
				if (maxX < contour[i].p2.x) maxX = contour[i].p2.x;
				pos_y += contour[i].p1.y;
				counter++;

				idxArr[i] = -1;
			}
		
		pos_y /= counter;
		return Line(Point(minX, pos_y), Point(maxX, pos_y));
	};
	Line processVertical(int* idxArr, int current_idx, vector<Line>& contour) {
		int minY = image.rows, maxY = -1, pos_x = 0, counter = 0;

		for (int i = 0; i < contour.size(); i++)
			if (idxArr[i] == current_idx) {
				if (minY > contour[i].p1.y) minY = contour[i].p1.y;
				if (minY > contour[i].p2.y) minY = contour[i].p2.y;
				if (maxY < contour[i].p1.y) maxY = contour[i].p1.y;
				if (maxY < contour[i].p2.y) maxY = contour[i].p2.y;
				pos_x += contour[i].p1.x;
				counter++;

				idxArr[i] = -1;
			}

		pos_x /= counter;
		return Line(Point(pos_x, minY), Point(pos_x, maxY));
	};
	bool isHorizontal(Line l) {
		int error = 4;
		return abs(l.p1.y - l.p2.y) < error && abs(l.p1.x - l.p2.x) > abs(l.p1.y - l.p2.y);
	}
	bool isVertical(Line l) {
		int error = 4;
		return abs(l.p1.x - l.p2.x) < error && abs(l.p1.x - l.p2.x) < abs(l.p1.y - l.p2.y);
	}
	double horizontalDistance(Line l1, Line l2) {
		return abs(l1.p1.y - l2.p1.y);
	}
	double verticalDistance(Line l1, Line l2) {
		return abs(l1.p1.x - l2.p1.x);
	}
	int min(int x, int y) {
		return x < y ? x : y;
	}
};
class KernelCalibration {
	int kernelSize;
	Mat image;
	Mat noEnvironmentImage;
public:
	KernelCalibration(String fileName, int kernelSize = 8, bool _fillTable = false, int difference = 1) {
		this->kernelSize = kernelSize;
		image = imread(fileName, IMREAD_GRAYSCALE);

		String dir = findDirName(fileName);
		noEnvironmentImage = environmentalFilter(image, difference);

		if (_fillTable)
			noEnvironmentImage = fillTable(noEnvironmentImage);

		imwrite(dir + "calibration" + extensionOf(fileName), noEnvironmentImage);
	}
private:
	Mat environmentalFilter(Mat& input, int difference) {
		Mat buf, output;
		Mat kernelErode = getStructuringElement(MORPH_RECT, Size(kernelSize, kernelSize)) * 255;
		Mat kernelDelate = getStructuringElement(MORPH_RECT, Size(kernelSize + difference, kernelSize + difference)) * 255;

		//remove lines with a thickness of 'difference'
		erode(input, buf, kernelErode);
		dilate(buf, output, kernelDelate);

		//fill the environment 
		floodFill(output, Point(1, 1), Scalar(0, 0, 0));
		floodFill(output, Point(kernelSize * 2, kernelSize * 2), Scalar(0, 0, 0));

		//clear the image
		Mat kernelOpening = getStructuringElement(MORPH_RECT, Size(kernelSize, kernelSize)) * 255;
		erode(output, buf, kernelOpening);
		dilate(buf, output, kernelOpening);

		return output;
	}
	void fillArea(Mat& inputImage, int startX, int startY, int endX, int endY) {
		for (int x = startX; x < endX; x++)
			for (int y = startY; y < endY; y++)
				inputImage.at<uchar>(y, x) = 0;
	}
	Mat fillTable(Mat& inputImage) {
		TABLE_FRAME tframe;
		LEFT_SHIFT_FRAME sframe;
		Mat noTableImage;
		inputImage.copyTo(noTableImage);
		int startX = noTableImage.cols * tframe.LeftUpFraction.x;
		int startY = noTableImage.rows * tframe.LeftUpFraction.y;
		fillArea(noTableImage, startX, startY, noTableImage.cols, noTableImage.rows);

		int endX = noTableImage.cols * sframe.LeftUpFraction.x;
		int endY = noTableImage.rows;
		fillArea(noTableImage, 0, 0, endX, endY);

		return noTableImage;
	}
};
class ScreenCalibration {
	const int rightOffset = 380;
	const int bottomOffset = 95;
	const int startX = 315;
	const int startY = 195;

	int kernelSize = 8;

public:
	ScreenCalibration(String fileName) {

		Mat image = imread(fileName, IMREAD_GRAYSCALE);
		image = processScreen(image);

		vector<vector<Point>> contours;
		findContours(image, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

		int* data = new int[3];
		processContour(contours, data);

		ofstream File("offset.txt");
		File << data[0]-2 << " " << data[1] << " " << data[2]+2 << std::endl;
		File.close();
	}
private:
	void fillArea(Mat& inputImage, int startX, int startY, int endX, int endY) {
		for (int x = startX; x < endX; x++)
			for (int y = startY; y < endY; y++)
				inputImage.at<uchar>(y, x) = 0;
	}
	Mat processScreen(Mat& input) {
		Mat binary;
		threshold(input, binary, 245, 255, THRESH_BINARY);

		fillArea(binary, 0, 0, startX, binary.rows);
		fillArea(binary, binary.cols - rightOffset, 0, binary.cols, binary.rows);
		fillArea(binary, startX, binary.rows - bottomOffset, binary.cols, binary.rows);
		fillArea(binary, startX, 0, binary.cols - rightOffset, startY);

		clearImage(binary);
		wallFilter(binary);

		return binary;
	}
	void clearImage(Mat& input) {
		Mat buf;
		Mat kernelOpening = getStructuringElement(MORPH_RECT, Size(kernelSize, kernelSize)) * 255;
		erode(input, buf, kernelOpening);
		dilate(buf, input, kernelOpening);
	}
	void wallFilter(Mat& input, int kernelMultiplier = 6) {
		int size = kernelSize * kernelMultiplier;
		Mat kernelClosing = getStructuringElement(MORPH_RECT, Size(size, size)) * 255;
		Mat buf;
		dilate(input, buf, kernelClosing);
		erode(buf, input, kernelClosing);
	}
	vector<Point>& maxContour(vector<vector<Point>>& contours) {
		double S = -1;
		int idx = -1;

		for (int i = 0; i < contours.size(); i++) {
			double curS = contourArea(contours[i]);
			if (curS > S) {
				S = curS;
				idx = i;
			}
		}
		return contours[idx];
	}
	void processContour(vector<vector<Point>> contours, int* out) {
		vector<Point> contour = maxContour(contours);

		int minX = contour[0].x;
		int minY = contour[0].y;
		int	maxX = minX;

		for (int i = 0; i < contour.size(); i++) {
			if (contour[i].x > maxX)
				maxX = contour[i].x;
			if (contour[i].x < minX)
				minX = contour[i].x;
			if (contour[i].y < minY)
				minY = contour[i].y;

		}

		out[0] = minX;
		out[1] = minY;
		out[2] = maxX - minX;
	}
};



JNIEXPORT void JNICALL Java_OIP_ImageProcessor_processImage(JNIEnv* env, jclass obj, jstring filePath, jstring noWallsImagePath, 
	jint kernelSize, jint minWallLen, jint contourExpansion) {

	const char* path = env->GetStringUTFChars(filePath, NULL);
	string nativeFilePath(path);
	path = env->GetStringUTFChars(noWallsImagePath, NULL);
	string nativeNoWallsImagePath(path);
	int nativeContExp = (int)contourExpansion;
	int nativeKernelSize = (int)kernelSize;
	int nativeMinWallLen = (int)minWallLen;

	String dirName = findDirName(nativeFilePath);

	ImageProcessor processor = ImageProcessor(nativeFilePath, nativeNoWallsImagePath, nativeKernelSize, nativeMinWallLen, nativeContExp);

	Mat image = processor.getImage();
	Mat innerCoverage = processor.innerContourCoverage();
	Mat outsideCoverage = processor.outsideContourCoverage();
	processor.writeImageToFile(innerCoverage, dirName + "innerCoverage" + extensionOf(nativeFilePath));
	processor.writeImageToFile(outsideCoverage, dirName + "outsideCoverage" + extensionOf(nativeFilePath));
	processor.writeImageToFile(image, dirName + "cropped" + extensionOf(nativeFilePath));
	
	processor.writeInnerContourToFile(dirName);
	processor.writeOutsideContourToFile(dirName);
}

JNIEXPORT void JNICALL Java_OIP_ImageProcessor_imageCalibration(JNIEnv* env, jclass obj, jstring filePath, jint kernelSize, jboolean fillTable, jint difference) {
	const char* path = env->GetStringUTFChars(filePath, NULL);
	string nativeFilePath(path);
	int nativeKernelSize = (int)kernelSize;
	int nativeDifference = (int)difference;
	bool nativeFillTable = (bool)fillTable;
	KernelCalibration kernelCalibration = KernelCalibration(nativeFilePath, nativeKernelSize, nativeFillTable, nativeDifference);
};

JNIEXPORT void JNICALL Java_OIP_ImageProcessor_screenCalibration(JNIEnv* env, jclass obj, jstring filePath) {
	const char* path = env->GetStringUTFChars(filePath, NULL);
	string nativeFilePath(path);

	ScreenCalibration screenCalibration = ScreenCalibration(nativeFilePath);
};
