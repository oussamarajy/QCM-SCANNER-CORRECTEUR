import cv2
import numpy as np


def rectContour(contours):
    rectCon = []
    for i in contours:
        area = cv2.contourArea(i)
        #print(area)
        if area>50:
            peri = cv2.arcLength(i, True)
            approx = cv2.approxPolyDP(i,0.02*peri, True)
            #print("C point", len(approx))
            if len(approx) == 4:
                rectCon.append(i)
    rectCon = sorted(rectCon, key=cv2.contourArea, reverse=True)
    return rectCon

def getCornerPonts(cont):
    peri = cv2.arcLength(cont, True)
    approx = cv2.approxPolyDP(cont, 0.02 * peri, True)
    return approx

def recorder(myPoints):
    myPoints = myPoints.reshape((4,2))
    myPointsNew = np.zeros((4,1,2), np.int32)
    add = myPoints.sum(1)
    #print(myPoints)
    #print(add)
    myPointsNew[0] = myPoints[np.argmin(add)]
    myPointsNew[3] = myPoints[np.argmax(add)]
    diff = np.diff(myPoints,axis=1)
    myPointsNew[1] = myPoints[np.argmin(diff)] # [w, 0]
    myPointsNew[2] = myPoints[np.argmax(diff)]  # [h, 0]
    return myPointsNew

def splitBoxes(img):
    rows = np.vsplit(img, 20)

    boxes = []
    for r in rows:
        cols = np.hsplit(r, 5)
        for box in cols:
            boxes.append(box)
            #cv2.imshow("split", box)


    return boxes

def showAnswers(img, myIndex, resultat_list, answers, questions, choices):
        a = int(img.shape[0]/questions)
        b = int(img.shape[1]/choices)

        for x in range(0, questions):
            myans = myIndex[x]
            cx = (myans*a)+a//2
            cy = (x*b) + b//2

            if resultat_list[x]==1:
                colo = (0,255,0)
            else:
                colo = (0,0,255)
                correct = answers[x]
                cv2.circle(img, ((correct * a) + a // 2, (x * b) + b // 2), 20, (0,255,0), cv2.FILLED)


            cv2.circle(img, (cx,cy), 30, colo, cv2.FILLED)

        return img

from difflib import SequenceMatcher

def similar(a, b):
    return SequenceMatcher(None, a, b).ratio()
