/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Button, Col, Row, Form} from 'reactstrap'
import {withRouter} from 'react-router'

import {loadData, updateAttribute} from "../utils/request"
import {nextPath} from "../history"
import {setMessage} from "../actions/actionCreators"

class Update extends Component {

    constructor() {
        super()
        this.state = {
            keyToUpdate: "",
            values: [""],
            initialDataReceived: false,
        }
        this.handleSubmit = this.handleSubmit.bind(this)
    }

    handleValueChange(idx, event) {
        let updatedValues = [...this.state.values]
        updatedValues[idx] = event.target.value
        this.setState({values: updatedValues})
    }

    static getDerivedStateFromProps(nextProps, state) {
        const stateUpdate = {}
        if (nextProps.match.params.keyToUpdate !== state.keyToUpdate) {
            stateUpdate.values = []
            state.initialDataReceived = false
        }
        if (!state.initialDataReceived) {
            if(nextProps.match.params.keyToUpdate in nextProps.attributes){
                stateUpdate.keyToUpdate = nextProps.match.params.keyToUpdate
                let array = []
                nextProps.attributes[nextProps.match.params.keyToUpdate].forEach((obj) => array.push(obj.name))
                stateUpdate.values = array
                stateUpdate.initialDataReceived = true
            }
        }
        return stateUpdate
    }

    loadAndRedirect = () => {
        loadData().then( () => {
            nextPath('/')
        })
    }

    handleSubmit(event) {
        event.preventDefault()
        updateAttribute(this.props.match.params.keyToUpdate, this.state.values.length === 0 ? [""] : this.state.values)
            .then((response) => {
                this.props.setMessage(response)
                this.loadAndRedirect()
            })
            .catch(this.props.setMessage)
    }

    addRow  = () => {
        this.setState((state) => (
            {
                values: [...this.state.values, ""]
            })
        )
    }

    deleteRow = (idx) => {
        this.setState((state) => {
            let newValues = [...this.state.values]
            newValues.splice(idx, 1)
            return {values: newValues}
        })
    }

    render() {
        const valueInputs =
            this.state.values.map((value, idx) =>
                <div className="input-group mb-3" key={idx}>
                    <input type="text"
                        value={value} className="form-control"
                        onChange={this.handleValueChange.bind(this, idx)}/>

                    <div className="input-group-append">
                        <button
                            className="btn btn-outline-secondary"
                            type="button"
                            onClick={this.deleteRow.bind(undefined, idx)}>
                                Remove
                        </button>
                    </div>
                </div>
            )

        return (
            <Col sm={{size: 6, offset: 3}}>
                <Form onSubmit={this.handleSubmit}>
                    <Row>
                        <Col lg={4}> Attribute name </Col>
                        <Col lg={8}>
                            <strong>
                                {this.props.match.params.keyToUpdate}
                            </strong>
                        </Col>
                    </Row>

                    <Row>
                        <Col lg={4} className="mt-3"> Values </Col>
                        <Col lg={8} className="mt-3">
                            {
                                this.state.values ? valueInputs : null
                            }
                        </Col>
                    </Row>

                    <div className="action-row to-the-right">
                        <Button className="btn btn-primary" onClick={this.addRow}>Add value</Button>
                        <Button className="btn btn-primary ml-2" type="submit" >Submit</Button>
                    </div>
                </Form>
            </Col>)
    }
}

export default withRouter(connect(
    (state) => ({
        attributes: state.identityData.attributes || {} ,
    }),
    (dispatch) => ({
        setMessage: (errorMessage) => dispatch(setMessage(errorMessage)),
    })
)(Update))
